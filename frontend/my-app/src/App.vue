<template>
  <div class="app-container">
    <!-- Header -->
    <el-header class="app-header">
      <h1>
        <el-icon style="vertical-align: middle; margin-right: 8px;"><Search /></el-icon>
        民法典AI助手
      </h1>
    </el-header>

    <!-- Main Content -->
    <el-main class="main-content">
      <!-- Search Card -->
      <el-card class="search-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Edit /></el-icon>
            <span>搜索查询</span>
          </div>
        </template>
        
        <div class="search-section">
          <el-input
            v-model="query"
            placeholder="请输入搜索内容"
            size="large"
            clearable
            @keyup.enter="searchAndAsk"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button 
            type="success" 
            size="large" 
            @click="searchAndAsk"
            :loading="streaming"
          >
            <template #icon><VideoPlay /></template>
            智能问答
          </el-button>
        </div>
      </el-card>

      <!-- Law Results Card for AI Search -->
      <el-card class="result-card" shadow="hover" v-if="lawResults.length > 0">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>相关法律条文 ({{ lawResults.length }})</span>
          </div>
        </template>
        
        <el-scrollbar height="400px">
          <div class="result-list">
            <el-card 
              v-for="(item, index) in lawResults" 
              :key="index" 
              class="result-item"
              shadow="never"
            >
              <div class="result-content" v-html="item"></div>
            </el-card>
          </div>
        </el-scrollbar>
      </el-card>

      <!-- Stream Card -->
      <el-card class="stream-card" shadow="hover" v-if="streamContent">
        <template #header>
          <div class="card-header">
            <el-icon class="streaming-icon" v-if="streaming"><VideoPlay /></el-icon>
            <el-icon v-else><Document /></el-icon>
            <span>AI解答</span>
            <el-tag v-if="streaming" type="success" size="small" style="margin-left: auto;">实时</el-tag>
            <el-tag v-else type="info" size="small" style="margin-left: auto;">已完成</el-tag>
          </div>
        </template>
        
        <el-scrollbar height="400px">
          <div class="stream-content">
            {{ streamContent }}
          </div>
        </el-scrollbar>
      </el-card>
    </el-main>
  </div>
</template>

<script>
import axios from 'axios';
import { Search, Edit, Document, VideoPlay } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

export default {
  components: {
    Search,
    Edit,
    Document,
    VideoPlay
  },
  data() {
    return {
      query: '',
      streamContent: '',
      streaming: false,
      lawResults: [] // 用于存储法律条文搜索结果
    };
  },
  methods: {
    // 整合搜索：先获取法律条文，再调用AI
    async searchAndAsk() {
      if (!this.query.trim()) {
        ElMessage.warning('请输入搜索内容');
        return;
      }
      
      this.streaming = true;
      this.streamContent = '';
      this.lawResults = [];
      
      try {
        // 1. 先调用ES搜索接口获取法律条文
        ElMessage.info('正在搜索相关法律条文...');
        const searchResponse = await axios.post('/api/searchCivilCode', { query: this.query });
        this.lawResults = searchResponse.data;
        
        if (this.lawResults.length > 0) {
          ElMessage.success(`找到 ${this.lawResults.length} 条相关法律条文`);
        } else {
          ElMessage.warning('未找到相关法律条文，仍将请求AI回答');
        }
        
        // 2. 构建包含法律条文的上下文
        let contextText = '根据以下相关法律条文，请回答用户的问题。\n\n';
        contextText += '相关法律条文：\n';
        
        if (this.lawResults.length > 0) {
          this.lawResults.forEach((item, index) => {
            contextText += `${index + 1}. ${item}\n\n`;
          });
        } else {
          contextText += '未找到相关法律条文。\n\n';
        }
        
        contextText += `用户问题：${this.query}\n\n`;
        contextText += '请基于以上法律条文给出专业的解答。';
        
        // 3. 调用AI流式接口，传入包含法律条文的上下文
        ElMessage.info('正在请求AI解答...');
        await this.streamWithContext(contextText);
        
      } catch (error) {
        console.error('搜索或AI请求失败:', error);
        ElMessage.error('请求失败，请稍后重试');
        this.streaming = false;
      }
    },
    
    // 使用上下文进行流式AI请求
    async streamWithContext(contextText) {
      return fetch('/api/streamData', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream'
        },
        body: JSON.stringify({ query: contextText })
      })
      .then(async (response) => {
        if (!response.body) {
          throw new Error('ReadableStream not supported in this browser.');
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';

        const processStream = async ({ done, value }) => {
          if (done) {
            console.log('Stream complete');
            this.streaming = false;
            ElMessage.success('AI回答完成');
            return;
          }

          // Decode the chunk
          const chunk = decoder.decode(value, { stream: true });
          buffer += chunk;
          
          // SSE 格式处理: data: xxx\n\n
          let boundaryIndex;
          while ((boundaryIndex = buffer.indexOf('\n\n')) !== -1) {
            const message = buffer.substring(0, boundaryIndex);
            buffer = buffer.substring(boundaryIndex + 2);
            
            if (message.trim()) {
              // 提取 data: 后的内容
              const lines = message.split('\n');
              for (const line of lines) {
                if (line.startsWith('data:')) {
                  // 不使用 trim()，保留空格
                  const content = line.substring(5);
                  if (content && content !== '[DONE]') {
                    // 立即更新内容（打字机效果）
                    this.streamContent += content;
                    
                    // 强制 DOM 更新并等待渲染
                    await this.$nextTick();
                    // 添加小延迟确保浏览器有时间渲染
                    await new Promise(resolve => setTimeout(resolve, 30));
                  }
                }
              }
            }
          }

          return reader.read().then(processStream);
        };

        return reader.read().then(processStream);
      });
    }
  }
};
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.app-header {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80px;
}

.app-header h1 {
  font-size: 32px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  display: flex;
  align-items: center;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

.search-card,
.result-card,
.stream-card {
  margin-bottom: 24px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.search-section {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-section .el-input {
  flex: 1;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  border-left: 4px solid #667eea;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.result-item:hover {
  border-left-color: #764ba2;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.result-content {
  font-size: 15px;
  line-height: 1.8;
  color: #444;
}

/* Custom style for <em> elements inside result-content */
.result-content :deep(em) {
  color: #ff4757;
  font-weight: 600;
  font-style: normal;
  background: rgba(255, 71, 87, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
}

.stream-content {
  background: #f0f5ff;
  border-left: 3px solid #52c41a;
  padding: 16px;
  border-radius: 4px;
  font-size: 15px;
  line-height: 1.8;
  color: #444;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.streaming-icon {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* Responsive Design */
@media (max-width: 768px) {
  .search-section {
    flex-direction: column;
  }
  
  .search-section .el-input {
    width: 100%;
  }
  
  .app-header h1 {
    font-size: 24px;
  }
}
</style>

