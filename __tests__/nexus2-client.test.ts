import nock from 'nock'
import lodash from 'lodash'
import { Nexus2Client } from '../src/nexus2-client'
import { BulkPromoteRequest, UploadFile } from '../src/interfaces'
import {
  STAGING_PROFILES_1,
  ERROR_NO_REPO_1,
  REPOSITORY_1
} from './data/mock-data'

describe('nexus2-client tests', () => {
  let client: Nexus2Client

  beforeEach(() => {
    client = new Nexus2Client({
      url: 'http://localhost:8082/nexus',
      username: 'admin',
      password: 'admin123',
      timeout: 0
    })
  })

  it('should return found profile id', async () => {
    nock('http://localhost:8082')
      .get('/nexus/service/local/staging/profiles')
      .reply(200, STAGING_PROFILES_1)
    const id = await client.getStagingProfileId('test')
    expect(id).toBe('2e32338b1a152')
  })

  it('should return repository', async () => {
    nock('http://localhost:8082')
      .get('/nexus/service/local/staging/repository/fake')
      .reply(200, REPOSITORY_1)
    const repository = await client.stagingRepository('fake')
    expect(repository.type).toBe('open')
  })

  it('should return error with no repo', async () => {
    nock('http://localhost:8082')
      .get('/nexus/service/local/staging/repository/fake')
      .reply(404, ERROR_NO_REPO_1)
    await expect(client.stagingRepository('fake')).rejects.toThrow()
  })

  it('should upload file into repository', async () => {
    nock('http://localhost:8082')
      .put(
        '/nexus/service/local/staging/deployByRepositoryId/fake/org%2Fexample/test.txt'
      )
      .reply(201)
    const uploadFile: UploadFile = {
      path: '__tests__/data/nexus/org/example/test.txt',
      name: 'test.txt',
      group: 'org%2Fexample'
    }
    await client.deployByRepository(uploadFile, 'fake')
  })

  it('should bulk promote and drop', async () => {
    nock('http://localhost:8082')
      .post('/nexus/service/local/staging/bulk/promote', body => {
        return lodash.isMatch(body, {
          data: {
            description: 'desc',
            stagedRepositoryIds: ['test'],
            autoDropAfterRelease: true
          }
        })
      })
      .reply(201)
    const request: BulkPromoteRequest = {
      data: {
        description: 'desc',
        stagedRepositoryIds: ['test'],
        autoDropAfterRelease: true
      }
    }
    await client.bulkPromoteStagingRepos(request)
  })

  it('should bulk promote and not drop', async () => {
    nock('http://localhost:8082')
      .post('/nexus/service/local/staging/bulk/promote', body => {
        return lodash.isMatch(body, {
          data: {
            description: 'desc',
            stagedRepositoryIds: ['test'],
            autoDropAfterRelease: false
          }
        })
      })
      .reply(201)
    const request: BulkPromoteRequest = {
      data: {
        description: 'desc',
        stagedRepositoryIds: ['test'],
        autoDropAfterRelease: false
      }
    }
    await client.bulkPromoteStagingRepos(request)
  })
})
