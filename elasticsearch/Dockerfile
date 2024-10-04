FROM docker.elastic.co/elasticsearch/elasticsearch:8.15.2

COPY elasticsearch/elasticsearch.yml /usr/share/elasticsearch/config/elasticsearch.yml

RUN elasticsearch-plugin install analysis-kuromoji
RUN elasticsearch-plugin install analysis-icu
RUN elasticsearch-plugin install analysis-smartcn
RUN elasticsearch-plugin install analysis-stempel

HEALTHCHECK --interval=5s --timeout=2s --retries=5 --start-period=10s \
  CMD curl --silent --fail 127.0.0.1:9200/_cluster/health || exit 1