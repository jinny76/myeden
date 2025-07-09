# 需求变更申请单 - CR-009-集成SearXNG内容分析

## 1. 变更基本信息
| 项目名称 | 变更编号 | 申请日期 | 申请人 | 变更类型 |
| -------- | -------- | -------- | ------ | -------- |
| [项目名] | CR-009   | 2025-07-09 | JINNI | 功能增强 |

## 2. 变更描述

### 2.1 变更背景
原方案通过SearXNG+爬虫采集网页内容，现发现SearXNG聚合搜索结果已能满足内容丰富性和时效性需求。直接基于SearXNG API返回的聚合内容进行标签化、AI分析与存储。

### 2.2 变更目标
1. 直接利用SearXNG聚合搜索结果作为内容源。
2. 对搜索结果进行标签提取、关键词分析和AI内容提纯。
3. 统一存储结构化内容，便于后续检索、推荐和分析。
4. 支持多种触发方式（标签、标题、名称等），实现内容自动补采。

### 2.3 变更内容
- 所有内容采集均通过SearXNG API完成。
- 对SearXNG返回的每条结果（标题、摘要、URL、来源等）进行分析剔除, 内容整合。
- 可选：对整合内容进一步用Dify等AI接口做内容提纯、标签化、结构化。
- 结果直接存入MongoDB，AI分析结果单独存储并与内容关联。
- 支持用户帖子（标签）、新闻/热点/音乐/影视（标题/名称）等多种内容补采触发方式。

## 3. 技术选型

- **SearXNG**：开源聚合搜索引擎，支持多源内容聚合，API接口丰富。
- **MongoDB**：半结构化数据存储，便于内容检索与扩展。
- **Dify（可选）**：AI内容提纯、摘要、标签化。

## 4. 数据采集与分析流程

1. **内容补采触发**
   - 用户帖子：以标签为关键词触发SearXNG搜索。
   - 资讯类内容（新闻、热点、音乐、影视等）：以标题/名称为关键词触发SearXNG搜索。
2. **SearXNG API检索**
   - 通过API获取聚合搜索结果（标题、摘要等结构化字段）。
4. **AI分析（可选）**
   - 可调用Dify等AI接口对处理背后的摘要/标题进一步提纯、生成AI标签、结构化实体等。
5. **存储**
   - 结果存入MongoDB的search_content集合，AI分析结果单独存储并与内容关联。

## 5. 数据库设计

### 5.1 search_content（搜索内容集合，批量结构）
```json
{
  "_id": ObjectId,
  "query": "原始搜索关键词",
  "source_type": "news|post|music|movie|hotspot|other",
  "results": [
    {
      "title": "搜索结果标题1",
      "summary": "搜索结果摘要1",
      "url": "https://xxx.com/xxx1",
      "source": "baidu"
      // 可扩展字段
    },
    {
      "title": "搜索结果标题2",
      "summary": "搜索结果摘要2",
      "url": "https://xxx.com/xxx2",
      "source": "bing"
    }
    // ...更多结果
  ],
  "created_at": ISODate,
  "status": "pending|analyzed|failed"
}
```

### 5.2 ai_analysis_result（AI分析结果表）
```json
{
  "_id": ObjectId,
  "content_id": ObjectId, // 指向search_content
  "source_type": "news|post|music|movie|hotspot|other", 
  "ai_summary": "AI生成的摘要",
  "ai_tags": ["AI标签1", "AI标签2"],
  "entities": [
    {"type": "person", "value": "张三", "source": ""},
    {"type": "location", "value": "北京", "source": ""}
  ],
  "sentiment": "positive|neutral|negative",
  "analysis_time": ISODate
}
```

## 5.3 Repository 查询实现方案

### SearchContentRepository（搜索内容仓库）

- 根据关键词模糊查询所有包含该关键词的搜索结果（title或summary）
- 根据内容类型查询
- 查询某一时间段内的内容
- 查询某个来源的所有结果
- 复合查询：按类型、关键词、时间段联合过滤

示例伪代码：
```java
// 根据关键词模糊查询
@Query("{'results': {$elemMatch: {$or: [{'title': {$regex: ?0, $options: 'i'}}, {'summary': {$regex: ?0, $options: 'i'}}]}}}")
List<SearchContent> findByKeywordInResults(String keyword);

// 根据内容类型查询
List<SearchContent> findBySourceType(String sourceType);

// 查询某一时间段内的内容
List<SearchContent> findByCreatedAtBetween(Date start, Date end);

// 查询某个来源的所有结果
@Query("{'results.source': ?0}")
List<SearchContent> findByResultSource(String source);

// 复合查询
@Query("{'source_type': ?0, 'results': {$elemMatch: {$or: [{'title': {$regex: ?1, $options: 'i'}}, {'summary': {$regex: ?1, $options: 'i'}}]}}, 'created_at': {$gte: ?2, $lte: ?3}}")
List<SearchContent> complexSearch(String sourceType, String keyword, Date start, Date end);
```

### AIAnalysisResultRepository（AI分析结果仓库）

- 根据内容ID查找AI分析结果
- 根据AI标签查找, 按分析时间段过滤

示例伪代码：
```java
List<AIAnalysisResult> findByContentId(String contentId);
List<AIAnalysisResult> findByAiTagsContaining(String tag, Date start, Date end);
```

> 所有方法均可结合Spring Data MongoDB自动实现，支持灵活组合与扩展。建议为results.title、results.summary等字段建立全文索引，提升检索效率。

## 6. Service层方法设计

### 6.1 SearchContentService（内容检索与管理服务）

- 根据关键词、类型、时间段检索内容
- 按来源检索内容
- 根据原始搜索关键词查找历史内容

示例伪代码：
```java
// 根据关键词、类型、时间段检索内容
List<SearchContent> search(String keyword, String sourceType, Date start, Date end);

// 按来源检索内容
List<SearchContent> searchBySource(String source);

// 根据原始搜索关键词查找历史内容
SearchContent findByQuery(String query);

```

### 6.2 关键业务流程方法：searchAndAnalyzeAndSave

**功能描述**：
根据给定的关键字和内容类型，自动完成搜索、结果处理、数据库入库、内容文件化、Dify Agent分析、分析结果入库等全流程。

**典型流程**：
1. 获取关键字和类型（参数：keyword, sourceType）
2. 发起SearXNG搜索，获取聚合搜索结果（title、summary、url、source等）
3. 结果处理（去重、过滤、整合等）
4. 批量存入search_content集合
5. 将本次搜索结果内容保存为文件
6. 调用Dify Agent分析（上传文件，触发AI分析）
7. 获取Dify分析结果，结构化后存入ai_analysis_result集合，并与本次内容关联

**方法签名与伪代码：**
```java
/**
 * 根据关键字和类型，自动完成搜索、处理、入库、文件化、AI分析与分析结果入库
 * @param keyword 搜索关键词
 * @param sourceType 内容类型
 * @return 分析结果对象
 */
AIAnalysisResult searchAndAnalyzeAndSave(String keyword, String sourceType);

// 伪代码流程
public AIAnalysisResult searchAndAnalyzeAndSave(String keyword, String sourceType) {
    // 1. 调用SearXNG API获取搜索结果
    List<SearchResult> results = searxngApi.search(keyword, sourceType);

    // 2. 结果预处理（去重、过滤等）
    List<SearchResult> processedResults = preprocess(results);

    // 3. 批量入库
    SearchContent content = saveSearchResults(keyword, sourceType, processedResults);

    // 4. 内容保存为文件
    File contentFile = saveResultsToFile(processedResults);

    // 5. 调用Dify Agent分析
    DifyResponse difyResponse = difyAgent.analyzeFile(contentFile);

    // 6. 解析分析结果并入库
    AIAnalysisResult analysisResult = saveAIAnalysisResult(content.getId(), sourceType, difyResponse);

    // 7. 返回分析结果
    return analysisResult;
}
```

**关键点说明：**
- 全流程自动化，一体化完成内容采集、处理、AI分析与结果入库。
- 每一步可单独抽象为子方法，便于维护和单元测试。
- 建议实现时对API调用、文件操作、入库等环节增加异常捕获与日志记录。
- 如分析量大，可将AI分析与入库部分异步处理或用消息队列解耦。

### 6.3 AIAnalysisService（AI分析与标签服务）

- 对指定内容集合发起AI分析
- 根据AI标签和时间段检索分析结果
- 根据内容ID获取AI分析结果

示例伪代码：
```java
// 对指定内容集合发起AI分析
AIAnalysisResult analyzeContent(String contentId);

// 根据AI标签和时间段检索分析结果
List<AIAnalysisResult> findByAiTagAndTime(String tag, Date start, Date end);

// 根据内容ID获取AI分析结果
AIAnalysisResult getResultByContentId(String contentId);
```

> Service层方法建议与Repository解耦，专注于业务流程和补采、AI调度等逻辑。可根据实际需求扩展分页、批量处理、异步任务等高级功能。

## 7. Controller层设计

Controller层主要负责接收HTTP请求、参数校验、调用Service层业务逻辑、返回标准响应。绝大多数业务处理和数据流转都应在Service层完成，Controller只需实现路由、参数转发和结果包装。

### 7.1 SearchContentController

- 根据关键词、类型、时间段检索内容
- 按来源检索内容
- 根据原始搜索关键词查找历史内容

示例伪代码：
```java
@RestController
@RequestMapping("/api/search-content")
public class SearchContentController {

    @Autowired
    private SearchContentService searchContentService;

    /**
     * 根据关键词、类型、时间段检索内容
     */
    @GetMapping("/search")
    public List<SearchContent> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
        return searchContentService.search(keyword, sourceType, start, end);
    }

    /**
     * 按来源检索内容
     */
    @GetMapping("/by-source")
    public List<SearchContent> searchBySource(@RequestParam String source) {
        return searchContentService.searchBySource(source);
    }

    /**
     * 根据原始搜索关键词查找历史内容
     */
    @GetMapping("/by-query")
    public SearchContent findByQuery(@RequestParam String query) {
        return searchContentService.findByQuery(query);
    }
}
```

### 7.2 AIAnalysisController

- 根据内容ID获取AI分析结果
- 根据AI标签和时间段检索分析结果

示例伪代码：
```java
@RestController
@RequestMapping("/api/ai-analysis")
public class AIAnalysisController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    /**
     * 根据内容ID获取AI分析结果
     */
    @GetMapping("/by-content")
    public AIAnalysisResult getResultByContentId(@RequestParam String contentId) {
        return aiAnalysisService.getResultByContentId(contentId);
    }

    /**
     * 根据AI标签和时间段检索分析结果
     */
    @GetMapping("/by-tag")
    public List<AIAnalysisResult> findByAiTagAndTime(
            @RequestParam String tag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end) {
        return aiAnalysisService.findByAiTagAndTime(tag, start, end);
    }
}
```

> Controller层只做参数接收、转发和响应包装，所有业务逻辑都在Service层。建议所有接口返回统一响应结构，便于前端处理和异常管理。可根据实际需求扩展分页、排序、权限校验等功能。

## 7.3 DifyService与PromptService设计

### DifyService（AI分析服务）

**职责**：
- 封装与Dify平台的API交互，包括文件上传、任务触发、结果轮询与获取等。
- 对外提供统一的AI分析接口，供业务层调用。

**典型方法与伪代码：**
```java
public interface DifyService {

   String callAiAnalysisAgent(File file, String prompt, String agentKey);

}
```

---

### PromptService（AI提示词与上下文管理服务）

**职责**：
- 负责生成、管理和优化传递给Dify的Prompt（提示词/上下文），提升AI分析的准确性和业务适配性。
- 可根据不同内容类型、业务场景动态生成Prompt。

**典型方法与伪代码：**
```java
public interface PromptService {
    
    /**
     * 上传内容文件到Dify并触发AI分析
     * @param file 内容文件
     * @param agentKey Dify Agent Key
     * @return 分析任务ID
     */
    AIAnalysisResult uploadAndTriggerAnalysis(File file, String agentKey);
    
}
```

---

**关键点说明**
- DifyService专注于与Dify平台的API交互，解耦业务与AI平台实现细节，便于后续切换或扩展AI服务。
- PromptService提升AI分析的业务适配性和可控性，是高质量AI输出的关键保障。
- 两者均建议实现异常处理、日志记录、参数校验等最佳实践。

## 任务拆解与开发TODO清单

1. 设计并实现search_content和ai_analysis_result的MongoDB实体类（POJO），包括字段、注释和索引。
2. 实现Repository接口，支持search_content和ai_analysis_result的基本增删查改及多维检索。
3. 实现SearchContentService接口及其实现类，包含内容检索、批量入库、内容文件化等基础方法。
4. 实现DifyService接口及其实现类，支持文件上传、prompt传递、Agent Key鉴权、AI分析结果获取。
5. 实现PromptService接口及其实现类，支持prompt生成、管理和优化。
6. 实现searchAndAnalyzeAndSave等核心业务流程方法，串联SearXNG搜索、内容处理、AI分析、入库全流程。
7. 实现Controller层的内容检索与AI分析结果查询接口，参数转发与结果包装。 