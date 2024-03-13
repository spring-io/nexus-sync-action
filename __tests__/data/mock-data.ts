export const STAGING_PROFILES_1 = `
{
  "data": [
    {
      "autoStagingDisabled": false,
      "closeRuleSets": [],
      "deployURI": "http://localhost:8082/nexus/service/local/staging/deploy/maven2",
      "dropNotifyCreator": true,
      "dropNotifyRoles": [],
      "finishNotifyCreator": true,
      "finishNotifyRoles": [],
      "id": "2e32338b1a152",
      "inProgress": false,
      "mode": "BOTH",
      "name": "test",
      "order": 0,
      "promoteRuleSets": [],
      "promotionNotifyCreator": true,
      "promotionNotifyRoles": [],
      "promotionTargetRepository": "releases",
      "properties": {
        "@class": "linked-hash-map"
      },
      "repositoriesSearchable": true,
      "repositoryTargetId": "1",
      "repositoryTemplateId": "default_hosted_release",
      "repositoryType": "maven2",
      "resourceURI": "http://localhost:8082/nexus/service/local/staging/profiles/2e32338b1a152",
      "targetGroups": [
        "public"
      ]
    }
  ]
}
`

export const STAGING_START_1 = `
{
  "data": {
    "description": "test",
    "stagedRepositoryId": "test-1004"
  }
}
`

export const REPOSITORY_1 = `
{
  "repositoryId": "test-xxx",
  "transitioning": false,
  "type": "open",
  "notifications": 0
}
`

export const REPOSITORY_2 = `
{
  "repositoryId": "test-xxx",
  "transitioning": false,
  "type": "closed",
  "notifications": 0
}
`

export const REPOSITORY_3 = `
{
  "repositoryId": "test-xxx",
  "transitioning": false,
  "type": "open",
  "notifications": 3
}
`

export const ACTIVITY_1 = `
[
  {
    "events": [
      {
        "name": "repositoryCreated",
        "properties": [
          {
            "name": "id",
            "value": "test-1068"
          },
          {
            "name": "user",
            "value": "admin"
          },
          {
            "name": "ip",
            "value": "127.0.0.1"
          }
        ],
        "severity": 0,
        "timestamp": "2021-02-21T09:48:52.360Z"
      }
    ],
    "name": "open",
    "started": "2021-02-21T09:48:52.328Z",
    "stopped": "2021-02-21T09:48:52.364Z"
  },
  {
    "events": [
      {
        "name": "rulesEvaluate",
        "properties": [
          {
            "name": "id",
            "value": "54d4f20a11594"
          },
          {
            "name": "rule",
            "value": "checksum-staging"
          },
          {
            "name": "rule",
            "value": "signature-staging"
          }
        ],
        "severity": 0,
        "timestamp": "2021-02-21T09:48:53.110Z"
      },
      {
        "name": "ruleEvaluate",
        "properties": [
          {
            "name": "typeId",
            "value": "signature-staging"
          }
        ],
        "severity": 0,
        "timestamp": "2021-02-21T09:48:53.112Z"
      },
      {
        "name": "ruleFailed",
        "properties": [
          {
            "name": "typeId",
            "value": "signature-staging"
          },
          {
            "name": "failureMessage",
            "value": "No public key: Key with id: (f7caaa2cdc112d56) was not able to be located on <a href=\\"http://pool.sks-keyservers.net:11371\\">http://pool.sks-keyservers.net:11371</a>. Upload your public key and try the operation again."
          },
          {
            "name": "failureMessage",
            "value": "No public key: Key with id: (f7caaa2cdc112d56) was not able to be located on <a href=\\"http://pool.sks-keyservers.net:11371\\">http://pool.sks-keyservers.net:11371</a>. Upload your public key and try the operation again."
          }
        ],
        "severity": 1,
        "timestamp": "2021-02-21T09:48:53.283Z"
      },
      {
        "name": "ruleEvaluate",
        "properties": [
          {
            "name": "typeId",
            "value": "checksum-staging"
          }
        ],
        "severity": 0,
        "timestamp": "2021-02-21T09:48:53.285Z"
      },
      {
        "name": "rulePassed",
        "properties": [
          {
            "name": "typeId",
            "value": "checksum-staging"
          }
        ],
        "severity": 0,
        "timestamp": "2021-02-21T09:48:53.291Z"
      },
      {
        "name": "rulesFailed",
        "properties": [
          {
            "name": "id",
            "value": "54d4f20a11594"
          },
          {
            "name": "failureCount",
            "value": "1"
          }
        ],
        "severity": 1,
        "timestamp": "2021-02-21T09:48:53.292Z"
      },
      {
        "name": "repositoryCloseFailed",
        "properties": [
          {
            "name": "id",
            "value": "test-1068"
          },
          {
            "name": "cause",
            "value": "com.sonatype.nexus.staging.StagingRulesFailedException: One or more rules have failed"
          }
        ],
        "severity": 1,
        "timestamp": "2021-02-21T09:48:53.293Z"
      }
    ],
    "name": "close",
    "started": "2021-02-21T09:48:53.109Z",
    "startedByIpAddress": "127.0.0.1",
    "startedByUserId": "admin",
    "stopped": "2021-02-21T09:48:53.293Z"
  }
]
`

export const ERROR_NO_REPO_1 = `
{
  "errors": [
      {
          "id": "*",
          "msg": "No such repository: fake"
      }
  ]
}
`
