## 后端：
保存题目，自动打分，ai问答

### api: //axios 
- 保存题目：POST /api/addQuestions
{id,content,answer,subject}
- 获取题目列表：GET /api/fetchQuestions

- 获取题目详情：GET /api/questions/:id

- 编辑题目: POST /api/editQuestions
{id,content,answer,subject}

- 打分：POST /api/questions/score
{id,content,answer,subject} 

- AI问答：POST /api/questions/ask
{question}

## 前端：
题目列表、题目详情、添加编辑题目、做题（AI打分）
### /
- 题目列表
### /question/:id
- 题目详情
### /addQuestion
- 添加题目
### /editQuestion/:id
- 编辑题目
### /scoreQuestion/:id
- 做题（AI打分）

## 数据库：
题目：id、content、answer、suject