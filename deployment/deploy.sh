#! /bin/bash

TAG=$1

# Prefix of file name is the tag.
DOCKERRUN_FILE="Dockerrun.aws.json"
DOCKERRUN_TEMPLATE="./deploy/dockerrun/Dockerrun.aws.json.template"
EB_EXTENSIONS_DIR=".ebextensions"
EB_ZIP="$TAG-eb-config.zip"

EB_BUCKET=$AWS_DEPLOY_BUCKET
APP_NAME=$AWS_APP_NAME
REGION=$AWS_REGION

# Replace tags in template file and create config file
sed -e "s/<TAG>/$TAG/" < $DOCKERRUN_TEMPLATE > $DOCKERRUN_FILE
zip -r $EB_ZIP $DOCKERRUN_FILE $EB_EXTENSIONS_DIR

aws s3 cp $EB_ZIP s3://$EB_BUCKET/$EB_ZIP

# Run AWS command to create a new EB application with label
aws elasticbeanstalk create-application-version --region=$REGION --application-name $APP_NAME --version-label $TAG --source-bundle S3Bucket=$EB_BUCKET,S3Key=$EB_ZIP
aws elasticbeanstalk update-environment --region=$REGION --application-name $APP_NAME --environment-name "streaming-user-segmentation" --version-label $TAG

# If app version is greater than 5, remove an old image
images=`aws elasticbeanstalk describe-application-versions --region=$REGION --application-name $APP_NAME | sed "/$TAG/d" | sed -n 's/"VersionLabel": "\([^"]*\)",/\1/p'`;
count=`echo "$images" | wc -l`
if [ "$count" > 5 ]; then
  image=`echo "$images" | tail -n 1`
  image=`echo "$image" | sed "s/^[ \t]*//"`
  aws elasticbeanstalk delete-application-version --version-label $image --application-name $APP_NAME --region=$REGION

  # clean quay
  quay_url="https://quay.io/api/v1/repository/crystalknows/streaming-user-segmentation/tag/${image}"
  quay_header="Authorization: Bearer $QUAY_TOKEN"
  curl --header "$quay_header" -X DELETE $quay_url
fi
