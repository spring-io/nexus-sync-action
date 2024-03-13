import nock from 'nock'
import { waitRepoState, stagingRepositoryActivity } from '../src/nexus-utils'
import { Nexus2Client } from '../src/nexus2-client'
import {
  REPOSITORY_1,
  REPOSITORY_2,
  REPOSITORY_3,
  ACTIVITY_1,
  ERROR_NO_REPO_1
} from './data/mock-data'

describe('nexus-utils tests', () => {
  let client: Nexus2Client

  beforeEach(() => {
    nock.cleanAll()
    client = new Nexus2Client({
      url: 'http://localhost:8082/nexus',
      username: 'admin',
      password: 'admin123',
      timeout: 0
    })
  })

  it('should succeed when waiting repo state', async () => {
    nock('http://localhost:8082')
      .persist()
      .get('/nexus/service/local/staging/repository/key')
      .reply(200, REPOSITORY_2)
    await waitRepoState(client, 'key', 'closed', 350, 100)
  })

  it('should reject when waiting repo state', async () => {
    nock('http://localhost:8082')
      .persist()
      .get('/nexus/service/local/staging/repository/key')
      .reply(200, REPOSITORY_1)
    await expect(
      waitRepoState(client, 'key', 'closed', 350, 100)
    ).rejects.toThrow('Timeout waiting state')
  })

  it('should reject with notifications', async () => {
    nock('http://localhost:8082')
      .persist()
      .get('/nexus/service/local/staging/repository/key')
      .reply(200, REPOSITORY_3)
    await expect(
      waitRepoState(client, 'key', 'closed', 350, 100)
    ).rejects.toThrow('Last operation failed with 3 notifications')
  })

  it('should succeed when repo goes away with autodrop', async () => {
    // assume with first request we get repo and
    // into second autodrop happened and we get 404
    nock('http://localhost:8082')
      .get('/nexus/service/local/staging/repository/key')
      .reply(200, REPOSITORY_1)
      .get('/nexus/service/local/staging/repository/key')
      .reply(404, ERROR_NO_REPO_1)
    await waitRepoState(client, 'key', 'closed', 350, 100)
  })

  it('should get repo activity', async () => {
    nock('http://localhost:8082')
      .persist()
      .get('/nexus/service/local/staging/repository/key/activity')
      .reply(200, ACTIVITY_1)
    const activities = await stagingRepositoryActivity(client, 'key')
    expect(activities.length).toBe(2)
  })
})
