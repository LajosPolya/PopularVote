# Popular Vote Deployer

This deployer deploys the Popular Vote application to the AWS cloud and consists of three deployment stacks; the foundation stack, the database stack, and the application stack.

## Foundation Stack

This stack contains all resources necessary for the foundation of the application. This includes the VPC, ECS cluster, and ECR repositories (although they are not currently in use for simplicity).

```shell
cdk deploy DeployFoundationStack
```

## Database Stack
This stack deploys the RDS cluster and the ECS task definition which will be used to run database migrations.

```shell
cdk deploy DeployDatabaseStack
```

Once deployed, the database migration can be executed using an ECS task deployed to the same VPC as the database.

```shell
// Do we need to assign public IP if it's deployed to Private with Egress Subnet? I don't think so
aws ecs run-task --task-definition popularVoteDbMigrationTask --cluster popularVoteCluster --network-configuration "awsvpcConfiguration={subnets=['<subnetId>'],securityGroups=['<securityGroupId>'],assignPublicIp=ENABLED}" --launch-type FARGATE
```

`<subnetId>` represents the name of the subnet the ECS task will be run in.
`<securityGroupId>` represents the name of the security group the ECS task will use.

All of these variables are output to the CLI. Running the above command creates a standalone ECS task inside of the Popular Vote cluster, the task executes the migration and then exits.

## Application Stack
The application stack deploys the Popular Vote API.

```shell
cdk deploy DeployApplicationStack
```

> [!CAUTION]
> AWS stacks may incur cost over time. Destroy all AWS stacks when they're not in use.


### Uploading the DB Migration image to ECR (not currently needed)

Use the instructions in [the database README](../database/README.md) to build the Docket image. Once built, upload it to
the repository.

> [!NOTE]
> THe following instructions were taken from [ECR's user guide](https://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html)

1. Login to ECR.

```shell
# replace `aws_account_id` with your AWS account ID
# replace all instances of `region` with your AWS region, for example, us-east-2
aws ecr get-login-password --region region | docker login --username AWS --password-stdin aws_account_id.dkr.ecr.region.amazonaws.com
```

2. Find the Popular Vote's Docker image.

```shell
docker images
```

3. Tag the image.

```shell
# replace `image_id` is the image's id returned by the `docker images`
# replace `aws_account_id` with your AWS account ID
# replace `region` with your AWS region, for example, us-east-2
# replace `my_repository` with your repository name, in this case it's popular-vote-db-migration
# replace `image_tag` with the an appropriate tag, for example, latest 
docker tag image_id aws_account_id.dkr.ecr.region.amazonaws.com/my_repository:image_tag
```

4. Push the image to the ECR repository.

```shell
# replace `aws_account_id` with your AWS account ID
# replace `region` with your AWS region, for example, us-east-2
# replace `my_repository` with your repository name, in this case it's popular-vote-db-migration
# replace `image_tag` with the an appropriate tag, for example, latest 
docker push aws_account_id.dkr.ecr.region.amazonaws.com/my_repository:image_tag
```

5. Destroy the stack

> [!CAUTION]
> AWS stacks may incur cost over time. Destroy all AWS stacks when they're not in use.

```shell
cdk destroy
```
