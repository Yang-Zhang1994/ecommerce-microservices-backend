FROM python:3.12-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY data ./data
COPY api ./api
COPY web ./web
COPY scripts ./scripts

ENV PROGRESS_CSV=/app/data/sample_progress.csv
ENV ANALYTICS_DB=/app/data/analytics.db
EXPOSE 8000
CMD ["uvicorn", "api.main:app", "--host", "0.0.0.0", "--port", "8000"]
