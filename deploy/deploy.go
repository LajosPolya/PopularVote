package main

import (
	"github.com/aws/aws-cdk-go/awscdk/v2"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsec2"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsecr"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsecs"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsecspatterns"
	"github.com/aws/aws-cdk-go/awscdk/v2/awselasticloadbalancingv2"
	"github.com/aws/aws-cdk-go/awscdk/v2/awslogs"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsrds"
	"github.com/aws/constructs-go/constructs/v10"
	"github.com/aws/jsii-runtime-go"
)

type DeployStackProps struct {
	awscdk.StackProps
}

type DeployDatabaseStackProps struct {
	awscdk.StackProps
	awsec2.IVpc
}

type DeployDatabaseStack struct {
	awscdk.Stack
	awsrds.DatabaseCluster
	awsecs.Cluster
}

func NewDatabaseStack(scope constructs.Construct, id string, props *DeployDatabaseStackProps) DeployDatabaseStack {
	var sprops awscdk.StackProps
	var vpc awsec2.IVpc
	if props != nil {
		sprops = props.StackProps
		vpc = props.IVpc
	}
	stack := awscdk.NewStack(scope, &id, &sprops)

	dbMigrationSg := awsec2.NewSecurityGroup(stack, jsii.String("popularVoteDbMigrationSg"), &awsec2.SecurityGroupProps{
		Vpc:               vpc,
		SecurityGroupName: jsii.String("popularVoteDbMigrationSg"),
	})

	dbSg := awsec2.NewSecurityGroup(stack, jsii.String("popularVoteDbSg"), &awsec2.SecurityGroupProps{
		Vpc:               vpc,
		SecurityGroupName: jsii.String("popularVoteDbSg"),
	})

	dbSg.AddIngressRule(awsec2.Peer_Ipv4(vpc.VpcCidrBlock()), awsec2.Port_AllTcp(), jsii.String("allInboundTcp"), jsii.Bool(false))
	dbSg.AddIngressRule(awsec2.Peer_SecurityGroupId(dbMigrationSg.SecurityGroupId(), nil), awsec2.Port_Tcp(jsii.Number(3306)), jsii.String("allInboundTcp"), jsii.Bool(false))
	s := make([]awsec2.ISecurityGroup, 1)
	s[0] = dbSg
	// add security group rule to allow inbound traffic

	defaultDatabaseName := "popularVote"
	dbCluster := awsrds.NewDatabaseCluster(stack, jsii.String("popularVoteDbCluster"), &awsrds.DatabaseClusterProps{
		Engine: awsrds.DatabaseClusterEngine_AuroraMysql(&awsrds.AuroraMysqlClusterEngineProps{
			Version: awsrds.AuroraMysqlEngineVersion_VER_3_07_1(),
		}),
		DefaultDatabaseName: &defaultDatabaseName,
		DeletionProtection:  jsii.Bool(false),
		RemovalPolicy:       awscdk.RemovalPolicy_DESTROY,
		SecurityGroups:      &s,
		Vpc:                 vpc,
		VpcSubnets: &awsec2.SubnetSelection{
			SubnetType: awsec2.SubnetType_PRIVATE_WITH_EGRESS,
		},
		Writer: awsrds.ClusterInstance_ServerlessV2(jsii.String("popularVoteServerlessInstance"), &awsrds.ServerlessV2ClusterInstanceProps{}),
	})

	awscdk.NewCfnOutput(stack, jsii.String("dbClusterEndpointHost"), &awscdk.CfnOutputProps{
		Value: dbCluster.ClusterEndpoint().Hostname(),
	})

	awscdk.NewCfnOutput(stack, jsii.String("dbClusterSecretArn"), &awscdk.CfnOutputProps{
		Value: dbCluster.Secret().SecretFullArn(),
	})

	cluster := awsecs.NewCluster(stack, jsii.String("popularVoteCluster"), &awsecs.ClusterProps{
		ClusterName: jsii.String("popularVoteCluster"),
		Vpc:         vpc,
	})

	taskDefinition := awsecs.NewTaskDefinition(stack, jsii.String("popularVoteDbMigrationTask"), &awsecs.TaskDefinitionProps{
		Cpu:           jsii.String("256"),
		Compatibility: awsecs.Compatibility_FARGATE,
		MemoryMiB:     jsii.String("512"),
	})

	// building the URL shouldn't be responsibility of the deployer. Maybe this should be done in the dockerfile?
	url := "jdbc:mysql://" + *(dbCluster.ClusterEndpoint().Hostname()) + ":3306?allowPublicKeyRetrieval=true"
	taskDefinition.AddContainer(jsii.String("popularVoteMigrationContainer"), &awsecs.ContainerDefinitionOptions{
		Image: awsecs.ContainerImage_FromAsset(jsii.String("../database/"), &awsecs.AssetImageProps{}),
		Environment: &map[string]*string{
			"FLYWAY_SCHEMAS": &defaultDatabaseName,
			"FLYWAY_URL":     &url,
		},
		Logging: awsecs.LogDrivers_AwsLogs(&awsecs.AwsLogDriverProps{
			StreamPrefix:  jsii.String("popularVoteMigrationLogs"),
			Mode:          awsecs.AwsLogDriverMode_NON_BLOCKING,
			MaxBufferSize: awscdk.Size_Mebibytes(jsii.Number(25)),
			LogRetention:  awslogs.RetentionDays_ONE_DAY,
		}),
		Secrets: &map[string]awsecs.Secret{
			"FLYWAY_USER":     awsecs.Secret_FromSecretsManager(dbCluster.Secret(), jsii.String("username")),
			"FLYWAY_PASSWORD": awsecs.Secret_FromSecretsManager(dbCluster.Secret(), jsii.String("password")),
		},
	})

	awscdk.NewCfnOutput(stack, jsii.String("taskDefinitionArn"), &awscdk.CfnOutputProps{
		Value: taskDefinition.TaskDefinitionArn(),
	})

	//  aws ecs run-task --task-definition DeployDatabaseStackpopularVoteDbMigrationTaskF0B0DA1D --cluster popularVoteCluster --network-configuration "awsvpcConfiguration={subnets=['subnet-0bdbf708446037956'],securityGroups=['sg-0d734f1e8f7e0b791'],assignPublicIp=ENABLED}" --launch-type FARGATE

	return DeployDatabaseStack{
		stack,
		dbCluster,
		// Move this to Foundation stack?
		cluster,
	}
}

type DeployFoundationStack struct {
	awscdk.Stack
	awsec2.IVpc
}

func NewFoundationStack(scope constructs.Construct, id string, props *DeployStackProps) DeployFoundationStack {
	var sprops awscdk.StackProps
	if props != nil {
		sprops = props.StackProps
	}
	stack := awscdk.NewStack(scope, &id, &sprops)

	appRepositoryName := jsii.String("popular-vote-app")
	appRepository := awsecr.NewRepository(stack, appRepositoryName, &awsecr.RepositoryProps{
		RemovalPolicy:  awscdk.RemovalPolicy_DESTROY,
		EmptyOnDelete:  jsii.Bool(true),
		RepositoryName: appRepositoryName,
	})

	dbMigrationRepositoryName := jsii.String("popular-vote-db-migration")
	dbMigrationRepository := awsecr.NewRepository(stack, dbMigrationRepositoryName, &awsecr.RepositoryProps{
		RemovalPolicy:  awscdk.RemovalPolicy_DESTROY,
		EmptyOnDelete:  jsii.Bool(true),
		RepositoryName: dbMigrationRepositoryName,
	})

	awscdk.NewCfnOutput(stack, jsii.String("appRepoName"), &awscdk.CfnOutputProps{
		Value: appRepository.RepositoryName(),
	})
	awscdk.NewCfnOutput(stack, jsii.String("appRepoUri"), &awscdk.CfnOutputProps{
		Value: appRepository.RepositoryUri(),
	})

	awscdk.NewCfnOutput(stack, jsii.String("dbMigrationRepoName"), &awscdk.CfnOutputProps{
		Value: dbMigrationRepository.RepositoryName(),
	})
	awscdk.NewCfnOutput(stack, jsii.String("dbMigrationRepoUri"), &awscdk.CfnOutputProps{
		Value: dbMigrationRepository.RepositoryUri(),
	})

	vpc := awsec2.NewVpc(stack, jsii.String("popularVoteVpc"), &awsec2.VpcProps{
		VpcName: jsii.String("popularVoteVpc"),
	})

	return DeployFoundationStack{
		stack,
		vpc,
	}
}

type DeployApplicationStackProps struct {
	awscdk.StackProps
	awsec2.IVpc
	awsecs.Cluster
	awsrds.DatabaseCluster
}

func NewApplicationStack(scope constructs.Construct, id string, props *DeployApplicationStackProps) awscdk.Stack {
	var sprops awscdk.StackProps
	var vpc awsec2.IVpc
	var cluster awsecs.Cluster
	var dbCluster awsrds.DatabaseCluster
	if props != nil {
		sprops = props.StackProps
		vpc = props.IVpc
		cluster = props.Cluster
		dbCluster = props.DatabaseCluster
	}
	stack := awscdk.NewStack(scope, &id, &sprops)

	/* NEVER DO THIS!
	The database should live in a Private Subnet to prevent unauthorized access from the public
	internet. This database is deployed to a public subnet so I can run migrations on it from my
	local machine but I'll need to update this in the future to improve security measures.
	*/
	sg := awsec2.NewSecurityGroup(stack, jsii.String("popularVoteAppSg"), &awsec2.SecurityGroupProps{
		Vpc:               vpc,
		SecurityGroupName: jsii.String("popularVoteAppSg"),
	})

	sg.AddIngressRule(awsec2.Peer_AnyIpv4(), awsec2.Port_AllTcp(), jsii.String("allInboundTcp"), jsii.Bool(false))
	s := make([]awsec2.ISecurityGroup, 1)
	s[0] = sg

	taskDefinition := awsecs.NewFargateTaskDefinition(stack, jsii.String("popularVoteAppTask"), &awsecs.FargateTaskDefinitionProps{
		Cpu:            jsii.Number(256),
		MemoryLimitMiB: jsii.Number(512),
	})

	// taskDefinition := awsecs.NewTaskDefinition(stack, jsii.String("popularVoteAppTask"), &awsecs.TaskDefinitionProps{
	// 	Cpu:           jsii.String("256"),
	// 	Compatibility: awsecs.Compatibility_FARGATE,
	// 	MemoryMiB:     jsii.String("512"),
	// })

	url := "r2dbc:mysql://" + *(dbCluster.ClusterEndpoint().Hostname()) + ":3306/popularVote?allowPublicKeyRetrieval=true"
	taskDefinition.AddContainer(jsii.String("popularVoteAppContainer"), &awsecs.ContainerDefinitionOptions{
		Image: awsecs.ContainerImage_FromAsset(jsii.String("../api/"), &awsecs.AssetImageProps{}),
		Environment: &map[string]*string{
			"SPRING_R2DBC_URL": &url,
		},
		Logging: awsecs.LogDrivers_AwsLogs(&awsecs.AwsLogDriverProps{
			StreamPrefix:  jsii.String("popularVoteAppLogs"),
			Mode:          awsecs.AwsLogDriverMode_NON_BLOCKING,
			MaxBufferSize: awscdk.Size_Mebibytes(jsii.Number(25)),
			LogRetention:  awslogs.RetentionDays_ONE_DAY,
		}),
		PortMappings: &[]*awsecs.PortMapping{{
			ContainerPort: jsii.Number(8080),
			AppProtocol:   awsecs.AppProtocol_Http(),
			// HostPort:      jsii.Number(80),
			Name: jsii.String("app_mapping"),
		}},
		Secrets: &map[string]awsecs.Secret{
			"SPRING_R2DBC_USERNAME": awsecs.Secret_FromSecretsManager(dbCluster.Secret(), jsii.String("username")),
			"SPRING_R2DBC_PASSWORD": awsecs.Secret_FromSecretsManager(dbCluster.Secret(), jsii.String("password")),
		},
	})

	service := awsecspatterns.NewApplicationLoadBalancedFargateService(stack, jsii.String("popularVoteApp"), &awsecspatterns.ApplicationLoadBalancedFargateServiceProps{
		Cluster:        cluster,
		DesiredCount:   jsii.Number(1),
		ServiceName:    jsii.String("popularVoteApi"),
		TaskDefinition: taskDefinition,
		AssignPublicIp: jsii.Bool(true),
		SecurityGroups: &s,
		// Vpc:            vpc,
		TaskSubnets: &awsec2.SubnetSelection{
			SubnetType: awsec2.SubnetType_PUBLIC,
		},
		HealthCheck: &awsecs.HealthCheck{
			Command: &[]*string{
				jsii.String("CMD-SHELL"),
				jsii.String("curl -f http://localhost/health || exit 1"),
			},
			// the properties below are optional
			Interval:    awscdk.Duration_Seconds(jsii.Number(5)),
			Retries:     jsii.Number(3),
			StartPeriod: awscdk.Duration_Minutes(jsii.Number(3)),
			Timeout:     awscdk.Duration_Minutes(jsii.Number(1)),
		},
	})
	service.TargetGroup().ConfigureHealthCheck(&awselasticloadbalancingv2.HealthCheck{
		HealthyHttpCodes: jsii.String("204"),
		Path:             jsii.String("/health"),
	})

	// awsecs.NewFargateService(stack, jsii.String("popularVoteApp"), &awsecs.FargateServiceProps{
	// 	Cluster:        cluster,
	// 	DesiredCount:   jsii.Number(1),
	// 	ServiceName:    jsii.String("popularVoteApi"),
	// 	TaskDefinition: taskDefinition,
	// 	AssignPublicIp: jsii.Bool(true),
	// 	SecurityGroups: &s,
	// 	VpcSubnets: &awsec2.SubnetSelection{
	// 		SubnetType: awsec2.SubnetType_PUBLIC,
	// 	},
	// })

	awscdk.NewCfnOutput(stack, jsii.String("appTaskDefinitionArn"), &awscdk.CfnOutputProps{
		Value: taskDefinition.TaskDefinitionArn(),
	})

	//  aws ecs run-task --task-definition DeployDatabaseStackpopularVoteDbMigrationTaskF0B0DA1D --cluster popularVoteCluster --network-configuration "awsvpcConfiguration={subnets=['subnet-0bdbf708446037956'],securityGroups=['sg-0d734f1e8f7e0b791'],assignPublicIp=ENABLED}" --launch-type FARGATE

	return stack
}

func main() {
	defer jsii.Close()

	app := awscdk.NewApp(nil)

	foundationStack := NewFoundationStack(app, "DeployFoundationStack", &DeployStackProps{
		awscdk.StackProps{
			Env: env(),
		},
	})

	databaseStack := NewDatabaseStack(app, "DeployDatabaseStack", &DeployDatabaseStackProps{
		awscdk.StackProps{
			Env: env(),
		},
		foundationStack.IVpc,
	})

	NewApplicationStack(app, "DeployApplicationStack", &DeployApplicationStackProps{
		awscdk.StackProps{
			Env: env(),
		},
		foundationStack.IVpc,
		databaseStack.Cluster,
		databaseStack.DatabaseCluster,
	})

	app.Synth(nil)
}

// env determines the AWS environment (account+region) in which our stack is to
// be deployed. For more information see: https://docs.aws.amazon.com/cdk/latest/guide/environments.html
func env() *awscdk.Environment {
	// If unspecified, this stack will be "environment-agnostic".
	// Account/Region-dependent features and context lookups will not work, but a
	// single synthesized template can be deployed anywhere.
	//---------------------------------------------------------------------------
	return nil

	// Uncomment if you know exactly what account and region you want to deploy
	// the stack to. This is the recommendation for production stacks.
	//---------------------------------------------------------------------------
	// return &awscdk.Environment{
	//  Account: jsii.String("123456789012"),
	//  Region:  jsii.String("us-east-1"),
	// }

	// Uncomment to specialize this stack for the AWS Account and Region that are
	// implied by the current CLI configuration. This is recommended for dev
	// stacks.
	//---------------------------------------------------------------------------
	// return &awscdk.Environment{
	//  Account: jsii.String(os.Getenv("CDK_DEFAULT_ACCOUNT")),
	//  Region:  jsii.String(os.Getenv("CDK_DEFAULT_REGION")),
	// }
}
