import fs from 'fs'
import * as https from 'https'
import axios, { AxiosInstance } from 'axios'
import {
  NexusServer,
  PromoteFinishRequest,
  PromoteFinishResponse,
  PromoteStartRequest,
  PromoteStartResponse,
  PromoteDropRequest,
  PromoteDropResponse,
  StagingProfilesResponse,
  UploadFile,
  Repository,
  BulkPromoteRequest,
  BulkPromoteResponse,
  Activity
} from './interfaces'
import { logDebug, logInfo, logWarn } from './logging'

/**
 * Client for nexus 2.x proving common operations.
 */
export class Nexus2Client {
  private instance: AxiosInstance

  constructor(private nexusServer: NexusServer) {
    this.instance = axios.create({
      auth: {
        username: nexusServer.username,
        password: nexusServer.password
      },
      timeout: nexusServer.timeout,
      maxBodyLength: Infinity,
      maxContentLength: Infinity,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json'
      },
      httpsAgent: new https.Agent({})
    })
  }

  /**
   * Get a matching staging profile id from a staging profile name.
   *
   * @param stagingProfileName
   */
  public getStagingProfileId(stagingProfileName: string): Promise<string> {
    return new Promise(async (resolve, reject) => {
      this.instance
        .get(`${this.nexusServer.url}/service/local/staging/profiles`)
        .then(r => {
          const stagingProfiles = r.data as StagingProfilesResponse
          const profile = stagingProfiles.data.find(
            p => p.name === stagingProfileName
          )
          if (profile) {
            resolve(profile.id)
          } else {
            reject('No profile found')
          }
        })
        .catch(e => {
          reject(e)
        })
    })
  }

  /**
   * Create a new staging repo were we can upload files. Takes stagingProfileId and PromoteStartRequest as arguments
   * and returns PromoteStartResponse as a promise.
   *
   * @param stagingProfileId the staging profile id
   * @param data promote start request
   */
  public createStagingRepo(
    stagingProfileId: string,
    data: PromoteStartRequest
  ): Promise<PromoteStartResponse> {
    return new Promise(async (resolve, reject) => {
      this.instance
        .post(
          `${this.nexusServer.url}/service/local/staging/profiles/${stagingProfileId}/start`,
          data
        )
        .then(r => {
          resolve(r.data as PromoteStartResponse)
        })
        .catch(e => {
          reject(e)
        })
    })
  }

  /**
   * Attempt to close a staging repo and returns PromoteFinishResponse as a Promise.
   *
   * @param stagingProfileId the staging profile id
   * @param data promote finish request
   */
  public closeStagingRepo(
    stagingProfileId: string,
    data: PromoteFinishRequest
  ): Promise<PromoteFinishResponse> {
    return new Promise(async (resolve, reject) => {
      this.instance
        .post(
          `${this.nexusServer.url}/service/local/staging/profiles/${stagingProfileId}/finish`,
          data
        )
        .then(r => {
          resolve(r.data as PromoteFinishResponse)
        })
        .catch(e => {
          reject(e)
        })
    })
  }

  /**
   * Promote a staging repo and returns PromotePromoteResponse as a Promise.
   *
   * @param data promote promote request
   */
  public bulkPromoteStagingRepos(
    data: BulkPromoteRequest
  ): Promise<BulkPromoteResponse> {
    return new Promise(async (resolve, reject) => {
      this.instance
        .post(
          `${this.nexusServer.url}/service/local/staging/bulk/promote`,
          data
        )
        .then(r => {
          resolve(r.data as BulkPromoteResponse)
        })
        .catch(e => {
          reject(e)
        })
    })
  }

  /**
   * Attempt to drop a staging repo and returns PromoteDropResponse as a Promise.
   *
   * @param stagingProfileId the staging profile id
   * @param data propote dro request
   */
  public dropStagingRepo(
    stagingProfileId: string,
    data: PromoteDropRequest
  ): Promise<PromoteDropResponse> {
    return new Promise(async (resolve, reject) => {
      this.instance
        .post(
          `${this.nexusServer.url}/service/local/staging/profiles/${stagingProfileId}/drop`,
          data
        )
        .then(r => {
          resolve(r.data as PromoteDropResponse)
        })
        .catch(e => {
          reject(e)
        })
    })
  }

  /**
   * Deploy a file into a staging repo and return Promise as a completion.
   *
   * @param uploadFile file to upload
   * @param repositoryId the repository id
   */
  public deployByRepository(
    uploadFile: UploadFile,
    repositoryId: string
  ): Promise<void> {
    logInfo(`Upload for file ${uploadFile.path} and repo ${repositoryId}`)
    logDebug(
      `File ${uploadFile.path} repositoryId=${repositoryId} group=${uploadFile.group} name=${uploadFile.name}`
    )
    return new Promise(async (resolve, reject) => {
      const stream = fs.createReadStream(uploadFile.path)
      logInfo(`Handling file ${uploadFile.path}`)
      this.instance
        .put(
          `${this.nexusServer.url}/service/local/staging/deployByRepositoryId/${repositoryId}/${uploadFile.group}/${uploadFile.name}`,
          stream,
          {
            headers: {
              'Content-Type': 'application/octet-stream',
              Pragma: 'no-cache',
              'Cache-Control': 'no-cache'
            }
          }
        )
        .then(r => {
          logInfo(`OK ${uploadFile.path}`)
          resolve()
        })
        .catch(e => {
          logWarn(`ERROR ${uploadFile.path}`)
          reject(e)
        })
    })
  }

  /**
   * Get info about a repository. Mostly used by tracking repository states.
   *
   * @param repositoryIdKey
   */
  public stagingRepository(repositoryIdKey: string): Promise<Repository> {
    return new Promise(async (resolve, reject) => {
      this.instance
        .get(
          `${this.nexusServer.url}/service/local/staging/repository/${repositoryIdKey}`
        )
        .then(r => {
          resolve(r.data as Repository)
        })
        .catch(e => {
          reject(e)
        })
    })
  }

  /**
   * Get info about a repository. Mostly used by tracking repository states.
   *
   * @param repositoryIdKey
   */
  public stagingRepositoryActivity(
    repositoryIdKey: string
  ): Promise<Activity[]> {
    return new Promise(async (resolve, reject) => {
      this.instance
        .get<Activity[]>(
          `${this.nexusServer.url}/service/local/staging/repository/${repositoryIdKey}/activity`
        )
        .then(r => {
          resolve(r.data)
        })
        .catch(e => {
          reject(e)
        })
    })
  }
}
