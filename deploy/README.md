# Welcome to your CDK Go project!

This is a blank project for CDK development with Go.

The `cdk.json` file tells the CDK toolkit how to execute your app.

## Useful commands

 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk synth`       emits the synthesized CloudFormation template
 * `go test`         run unit tests

## The Popular Vote deployer

```shell
cdk deploy
```
This command deploys the application, which for now only deploys two AWS ECR repositories. The repositories are meant
to store Docker images.

### Uploading the DB Migration image to ECR

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
