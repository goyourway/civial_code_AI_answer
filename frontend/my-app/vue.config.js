const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 3000, // 将端口号改为 3000 或其他未占用的端口
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        // Remove pathRewrite or set it conditionally if backend supports /api paths
      }
    }
  }
})
