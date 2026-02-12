import http from '@/utils/httpRequest.js'

/**
 * Get S3 presigned URL for direct PUT upload.
 * Returns { uploadUrl, fileUrl, key }.
 */
export function getS3PresignedUrl(filename) {
  return new Promise((resolve, reject) => {
    http({
      url: http.adornUrl('/third-party/s3/presigned-url'),
      method: 'get',
      params: http.adornParams({ filename })
    })
      .then(({ data }) => {
        if (data.code === 0 && data.data) {
          resolve(data.data)
        } else {
          reject(new Error(data.msg || 'Failed to get presigned URL'))
        }
      })
      .catch(reject)
  })
}
