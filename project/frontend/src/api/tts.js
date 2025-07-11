import request from '@/utils/request'

/**
 * 语音合成
 * @param {string} text 文本
 * @param {string} voiceType 语音类型
 * @returns Promise
 */
export function tts(text, voiceType) {
  return request({
    url: `/tts`,
    method: 'post',
    data: {
      "request": {"text": text},
      "audio": {"voice_type": voiceType}
    }
  })
}