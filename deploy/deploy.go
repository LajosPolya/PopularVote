package main

import (
	"github.com/aws/aws-cdk-go/awscdk/v2"
	// "github.com/aws/aws-cdk-go/awscdk/v2/awssqs"
	"github.com/aws/aws-cdk-go/awscdk/v2/awsecr"
	"github.com/aws/constructs-go/constructs/v10"
	"github.com/aws/jsii-runtime-go"
)

type DeployStackProps struct {
	awscdk.StackProps
}

func NewDeployStack(scope constructs.Construct, id string, props *DeployStackProps) awscdk.Stack {
	var sprops awscdk.StackProps
	if props != nil {
		sprops = props.StackProps
	}
	stack := awscdk.NewStack(scope, &id, &sprops)

	// The code that defines your stack goes here

	// example resource
	// queue := awssqs.NewQueue(stack, jsii.String("DeployQueue"), &awssqs.QueueProps{
	// 	VisibilityTimeout: awscdk.Duration_Seconds(jsii.Number(300)),
	// })

	appRepository := awsecr.NewRepository(stack, jsii.String("PopularVoteApp"), &awsecr.RepositoryProps{
		RemovalPolicy: awscdk.RemovalPolicy_DESTROY,
		EmptyOnDelete: jsii.Bool(true),
	})

	dbMigrationRepository := awsecr.NewRepository(stack, jsii.String("PopularVoteDbMigration"), &awsecr.RepositoryProps{
		RemovalPolicy: awscdk.RemovalPolicy_DESTROY,
		EmptyOnDelete: jsii.Bool(true),
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

	return stack
}

func main() {
	defer jsii.Close()

	app := awscdk.NewApp(nil)

	NewDeployStack(app, "DeployStack", &DeployStackProps{
		awscdk.StackProps{
			Env: env(),
		},
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
