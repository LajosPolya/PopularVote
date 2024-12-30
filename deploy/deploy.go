package main

import (
	"github.com/aws/aws-cdk-go/awscdk/v2"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsec2"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsecr"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsecs"
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

func NewDatabaseStack(scope constructs.Construct, id string, props *DeployDatabaseStackProps) awscdk.Stack {
	var sprops awscdk.StackProps
	var vpc awsec2.IVpc
	if props != nil {
		sprops = props.StackProps
		vpc = props.IVpc
	}
	stack := awscdk.NewStack(scope, &id, &sprops)

	/* NEVER DO THIS!
	The database should live in a Private Subnet to prevent unauthorized access from the public
	internet. This database is deployed to a public subnet so I can run migrations on it from my
	local machine but I'll need to update this in the future to improve security measures.
	*/
	sg := awsec2.NewSecurityGroup(stack, jsii.String("popularVoteDbSg"), &awsec2.SecurityGroupProps{
		Vpc:               vpc,
		SecurityGroupName: jsii.String("popularVoteDbSg"),
	})

	sg.AddIngressRule(awsec2.Peer_AnyIpv4(), awsec2.Port_AllTcp(), jsii.String("allInboundTcp"), jsii.Bool(false))
	s := make([]awsec2.ISecurityGroup, 1)
	s[0] = sg
	// add security group rule to allow inbound traffic
	dbCluster := awsrds.NewDatabaseCluster(stack, jsii.String("popularVoteDbCluster"), &awsrds.DatabaseClusterProps{
		Engine: awsrds.DatabaseClusterEngine_AuroraMysql(&awsrds.AuroraMysqlClusterEngineProps{
			Version: awsrds.AuroraMysqlEngineVersion_VER_3_07_1(),
		}),
		DefaultDatabaseName: jsii.String("popularVote"),
		DeletionProtection:  jsii.Bool(false),
		RemovalPolicy:       awscdk.RemovalPolicy_DESTROY,
		SecurityGroups:      &s,
		Vpc:                 vpc,
		VpcSubnets: &awsec2.SubnetSelection{
			SubnetType: awsec2.SubnetType_PUBLIC,
		},
		Writer: awsrds.ClusterInstance_ServerlessV2(jsii.String("popularVoteServerlessInstance"), &awsrds.ServerlessV2ClusterInstanceProps{}),
	})

	awscdk.NewCfnOutput(stack, jsii.String("dbClusterEndpointHost"), &awscdk.CfnOutputProps{
		Value: dbCluster.ClusterEndpoint().Hostname(),
	})

	awscdk.NewCfnOutput(stack, jsii.String("dbClusterSecretArn"), &awscdk.CfnOutputProps{
		Value: dbCluster.Secret().SecretFullArn(),
	})

	awsecs.NewCluster(stack, jsii.String("popularVoteCluster"), &awsecs.ClusterProps{
		ClusterName: jsii.String("popularVoteCluster"),
		Vpc:         vpc,
	})

	taskDefinition := awsecs.NewTaskDefinition(stack, jsii.String("popularVoteDbMigrationTask"), &awsecs.TaskDefinitionProps{
		Cpu:           jsii.String("256"),
		Compatibility: awsecs.Compatibility_FARGATE,
		MemoryMiB:     jsii.String("512"),
	})

	containerEnv := make(map[string]*string)
	containerEnv["FLYWAY_USER"] = dbCluster.Secret().SecretValueFromJson(jsii.String("username")).UnsafeUnwrap()
	containerEnv["FLYWAY_PASSWORD"] = dbCluster.Secret().SecretValueFromJson(jsii.String("password")).UnsafeUnwrap()
	url := "jdbc:mysql://" + *(dbCluster.ClusterEndpoint().Hostname()) + ":3306/popularVote?allowPublicKeyRetrieval=true"
	containerEnv["FLYWAY_URL"] = &url
	taskDefinition.AddContainer(jsii.String("popularVoteMigrationContainer"), &awsecs.ContainerDefinitionOptions{
		Image:       awsecs.ContainerImage_FromAsset(jsii.String("../database/"), &awsecs.AssetImageProps{}),
		Environment: &containerEnv,
		Logging: awsecs.LogDrivers_AwsLogs(&awsecs.AwsLogDriverProps{
			StreamPrefix:  jsii.String("popularVoteMigrationLogs"),
			Mode:          awsecs.AwsLogDriverMode_NON_BLOCKING,
			MaxBufferSize: awscdk.Size_Mebibytes(jsii.Number(25)),
			LogRetention:  awslogs.RetentionDays_ONE_DAY,
		}),
	})

	awscdk.NewCfnOutput(stack, jsii.String("taskDefinitionArn"), &awscdk.CfnOutputProps{
		Value: taskDefinition.TaskDefinitionArn(),
	})

	awscdk.NewCfnOutput(stack, jsii.String("secretValue"), &awscdk.CfnOutputProps{
		Value: dbCluster.Secret().SecretValueFromJson(jsii.String("password")).UnsafeUnwrap(),
	})

	//   aws ecs run-task --task-definition DeployDatabaseStackpopularVoteDbMigrationTaskF0B0DA1D --cluster popularVoteCluster --network-configuration "awsvpcConfiguration={subnets=['subnet-0ebfd503a09beeafb'],securityGroups=['sg-081ec50683589e6ea']}" --launch-type FARGATE

	return stack
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

func main() {
	defer jsii.Close()

	app := awscdk.NewApp(nil)

	foundationStack := NewFoundationStack(app, "DeployFoundationStack", &DeployStackProps{
		awscdk.StackProps{
			Env: env(),
		},
	})

	NewDatabaseStack(app, "DeployDatabaseStack", &DeployDatabaseStackProps{
		awscdk.StackProps{
			Env: env(),
		},
		foundationStack.IVpc,
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
