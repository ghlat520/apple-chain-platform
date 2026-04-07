# Apple-Chain-Platform 大数据栈状态快照

**采集时间**：2026-04-07 16:03

## 容器清单

```
NAMES                         STATUS                             PORTS
apple-datahub-frontend        Up 1h (healthy)                    0.0.0.0:9002->9002/tcp
apple-datahub-gms             Up (starting)                      0.0.0.0:18081->8080/tcp
apple-datahub-mysql           Up 1h (healthy)                    3306/tcp (internal)
apple-datahub-neo4j           Up 1h (healthy)                    7473-7474/tcp, 7687/tcp (internal)
apple-datahub-elasticsearch   Up 1h (healthy)                    9200/9300/tcp (internal)
apple-dolphinscheduler        Up 1h                              0.0.0.0:12345->12345/tcp, 0.0.0.0:25333->25333/tcp
apple-spark-worker            Up 1h                              (internal)
apple-redis-bigdata           Up 1h                              0.0.0.0:6380->6379/tcp
apple-clickhouse              Up 1h (healthy)                    0.0.0.0:8123->8123/tcp, 0.0.0.0:9000->9000/tcp
apple-kafka                   Up 1h (healthy)                    0.0.0.0:9092-9093->9092-9093/tcp
apple-spark-master            Up 1h                              0.0.0.0:7077->7077/tcp, 0.0.0.0:18080->8080/tcp
```

## UI 入口

| 服务 | URL | 凭据 |
|---|---|---|
| DataHub UI | http://localhost:9002 | datahub / datahub |
| DolphinScheduler | http://localhost:12345 | admin / dolphinscheduler123 |
| Spark Master UI | http://localhost:18080 | — |
| ClickHouse HTTP | http://localhost:8123 | apple / apple123 |

## 健康验证命令

```bash
# DataHub GMS 版本
curl -sf http://localhost:18081/config | head -10

# DataHub Frontend
curl -sfI http://localhost:9002/

# ClickHouse 连通
curl -sf 'http://localhost:8123/?query=SELECT+version()'

# Kafka 主题列表（从 broker 容器内）
docker exec apple-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# DataHub MySQL schema
docker exec apple-datahub-mysql mysql -udatahub -pdatahub -e 'SHOW TABLES FROM datahub;'
```

## 已知问题

**DataHub GMS ebean classpath bug**：
- 症状：`POST /openapi/openlineage/api/v1/lineage` 返回 HTTP 500
- 根因：`No service implementation found for interface io.ebean.service.SpiRawSqlService`
- 影响：OpenLineage 事件无法持久化
- 修复路径：升级 `acryldata/datahub-gms` 到 v0.14.x 或更高

**DataHub SystemUpdate 未完全完成**：
- 7 步中的前 5 步（ES 索引）已完成
- 第 6-7 步（MySQL 回填）因 ebean bug 失败
- 可与 DataHub 镜像升级一并解决

## 重启命令

```bash
cd /Applications/soft/CodeSpace/apple-chain-platform

# 核心大数据栈（Kafka/ClickHouse/Spark/DS/Redis）
docker compose -f docker-compose-bigdata.yml up -d

# + 治理栈（DataHub 5 服务）
docker compose -f docker-compose-bigdata.yml --profile governance up -d

# + 采集栈（SeaTunnel）
docker compose -f docker-compose-bigdata.yml --profile collection up -d

# 全停（不删 volume）
docker compose -f docker-compose-bigdata.yml --profile governance --profile collection down
```

## 卷清单

```
kafka-data, clickhouse-data, clickhouse-logs, ds-data, spark-data,
canal-conf, st-logs, datahub-mysql-data, datahub-es-data, datahub-neo4j-data
```
