# Production Deployment Guide

**Application:** Repository Maintainability Index (RMI)  
**Version:** 1.0.0  
**Last Updated:** 2024  
**Target Audience:** DevOps Engineers, SREs

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Deployment Options](#deployment-options)
4. [JAR Deployment](#jar-deployment)
5. [Docker Deployment](#docker-deployment)
6. [Kubernetes Deployment](#kubernetes-deployment)
7. [AWS Deployment](#aws-deployment)
8. [GCP Deployment](#gcp-deployment)
9. [Azure Deployment](#azure-deployment)
10. [Configuration Management](#configuration-management)
11. [Security Hardening](#security-hardening)
12. [Monitoring Setup](#monitoring-setup)
13. [Rollback Procedures](#rollback-procedures)
14. [Troubleshooting](#troubleshooting)

---

## Overview

This guide provides comprehensive instructions for deploying RMI in production environments across multiple platforms.

### Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  Deployment Options                     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐     │
│  │   JAR    │  │  Docker  │  │   Kubernetes     │     │
│  │ Direct   │  │Container │  │   Orchestration  │     │
│  │Execution │  │          │  │                  │     │
│  └──────────┘  └──────────┘  └──────────────────┘     │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │        Cloud Platforms                           │  │
│  │  ┌────────┐  ┌────────┐  ┌────────┐            │  │
│  │  │  AWS   │  │  GCP   │  │ Azure  │            │  │
│  │  └────────┘  └────────┘  └────────┘            │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Prerequisites

### General Requirements
- Java 17 or higher (LTS recommended)
- GitHub personal access token
- OpenRouter API key (optional, for LLM features)
- Network access to GitHub API (api.github.com)
- Network access to OpenRouter API (openrouter.ai) if using LLM

### Infrastructure Requirements
| Resource | Minimum | Recommended | Production |
|----------|---------|-------------|------------|
| CPU | 0.5 cores | 1 core | 2 cores |
| Memory | 128 MB | 256 MB | 512 MB |
| Disk | 100 MB | 500 MB | 1 GB |
| Network | 10 Mbps | 50 Mbps | 100 Mbps |

### Software Dependencies
- Java Runtime Environment (JRE) 17+
- Docker 20.10+ (for containerized deployment)
- Kubernetes 1.24+ (for orchestration)
- Helm 3.0+ (for Kubernetes package management)

---

## Deployment Options

### 1. JAR Deployment ✅ Simple, Traditional
**Best For:** Development, small-scale, VM-based deployments  
**Complexity:** Low  
**Scalability:** Manual

### 2. Docker Deployment ✅ Containerized
**Best For:** Production, CI/CD, consistent environments  
**Complexity:** Medium  
**Scalability:** Horizontal (manual)

### 3. Kubernetes Deployment ✅ Orchestrated
**Best For:** Enterprise, high-availability, auto-scaling  
**Complexity:** High  
**Scalability:** Horizontal (automatic)

---

## JAR Deployment

### Step 1: Build Application

```bash
# Clone repository
git clone https://github.com/your-org/repository-maintainability-index.git
cd repository-maintainability-index

# Build with Maven
mvn clean package -DskipTests

# Verify JAR
ls -lh target/repo-maintainability-index-1.0.0.jar
```

### Step 2: Deploy to Server

```bash
# Copy JAR to production server
scp target/repo-maintainability-index-1.0.0.jar user@prod-server:/opt/rmi/

# SSH to server
ssh user@prod-server

# Create directory structure
sudo mkdir -p /opt/rmi/{bin,config,logs}
sudo mv /opt/rmi/repo-maintainability-index-1.0.0.jar /opt/rmi/bin/
```

### Step 3: Configure Environment

```bash
# Create environment file
sudo nano /opt/rmi/config/.env
```

**File: `/opt/rmi/config/.env`**
```bash
# GitHub API Configuration
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# OpenRouter API Configuration (optional)
OPENROUTER_API_KEY=sk-or-xxxxxxxxxxxxxxxxxxxxxxxxxxxx
OPENROUTER_MODEL=openai/gpt-3.5-turbo

# Logging Configuration
LOG_LEVEL=INFO
LOG_FILE=/opt/rmi/logs/rmi.log

# JVM Configuration
JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
```

### Step 4: Create Systemd Service

**File: `/etc/systemd/system/rmi.service`**

```ini
[Unit]
Description=Repository Maintainability Index Service
After=network.target

[Service]
Type=simple
User=rmi
Group=rmi
WorkingDirectory=/opt/rmi
EnvironmentFile=/opt/rmi/config/.env
ExecStart=/usr/bin/java -Dfile.encoding=UTF-8 ${JAVA_OPTS} -jar /opt/rmi/bin/repo-maintainability-index-1.0.0.jar analyze %i
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/rmi/logs/rmi.log
StandardError=append:/opt/rmi/logs/rmi-error.log

# Security
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/rmi/logs

[Install]
WantedBy=multi-user.target
```

### Step 5: Start Service

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service
sudo systemctl enable rmi.service

# Start service
sudo systemctl start rmi.service

# Check status
sudo systemctl status rmi.service

# View logs
journalctl -u rmi.service -f
```

### Step 6: Configure Log Rotation

**File: `/etc/logrotate.d/rmi`**

```
/opt/rmi/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 rmi rmi
    sharedscripts
    postrotate
        systemctl reload rmi.service > /dev/null 2>&1 || true
    endscript
}
```

---

## Docker Deployment

### Step 1: Build Docker Image

```bash
# Build image
docker build -t rmi-app:1.0.0 .

# Tag for registry
docker tag rmi-app:1.0.0 your-registry.com/rmi-app:1.0.0

# Push to registry
docker push your-registry.com/rmi-app:1.0.0
```

### Step 2: Create Docker Compose Configuration

**File: `docker-compose.prod.yml`**

```yaml
version: '3.8'

services:
  rmi:
    image: your-registry.com/rmi-app:1.0.0
    container_name: rmi-app
    restart: unless-stopped
    
    environment:
      - GITHUB_TOKEN=${GITHUB_TOKEN}
      - OPENROUTER_API_KEY=${OPENROUTER_API_KEY}
      - OPENROUTER_MODEL=${OPENROUTER_MODEL:-openai/gpt-3.5-turbo}
      - LOG_LEVEL=${LOG_LEVEL:-INFO}
    
    volumes:
      - ./logs:/logs
      - ./cache:/cache
    
    networks:
      - rmi-network
    
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
    
    healthcheck:
      test: ["CMD", "java", "-jar", "/app/rmi.jar", "--version"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

networks:
  rmi-network:
    driver: bridge

volumes:
  logs:
  cache:
```

### Step 3: Deploy with Docker Compose

```bash
# Create .env file
cat > .env <<EOF
GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
OPENROUTER_API_KEY=sk-or-xxxxxxxxxxxxxxxxxxxxxxxxxxxx
OPENROUTER_MODEL=openai/gpt-3.5-turbo
LOG_LEVEL=INFO
EOF

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Stop services
docker-compose -f docker-compose.prod.yml down
```

### Step 4: Docker Swarm Deployment (Optional)

```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.prod.yml rmi

# List services
docker stack services rmi

# View logs
docker service logs rmi_rmi -f

# Scale service
docker service scale rmi_rmi=3

# Remove stack
docker stack rm rmi
```

---

## Kubernetes Deployment

### Step 1: Create Namespace

**File: `k8s/namespace.yaml`**

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: rmi
  labels:
    app: rmi
    environment: production
```

### Step 2: Create Secrets

```bash
# Create secret for credentials
kubectl create secret generic rmi-credentials \
  --from-literal=github-token='ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx' \
  --from-literal=openrouter-api-key='sk-or-xxxxxxxxxxxxxxxxxxxxxxxxxxxx' \
  --namespace=rmi
```

**Or use YAML:**

**File: `k8s/secrets.yaml`**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: rmi-credentials
  namespace: rmi
type: Opaque
stringData:
  github-token: ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
  openrouter-api-key: sk-or-xxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### Step 3: Create ConfigMap

**File: `k8s/configmap.yaml`**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: rmi-config
  namespace: rmi
data:
  OPENROUTER_MODEL: "openai/gpt-3.5-turbo"
  LOG_LEVEL: "INFO"
  JAVA_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC -Dfile.encoding=UTF-8"
```

### Step 4: Create Deployment

**File: `k8s/deployment.yaml`**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rmi
  namespace: rmi
  labels:
    app: rmi
    version: "1.0.0"
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rmi
  template:
    metadata:
      labels:
        app: rmi
        version: "1.0.0"
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/metrics"
    spec:
      serviceAccountName: rmi
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      
      containers:
      - name: rmi
        image: your-registry.com/rmi-app:1.0.0
        imagePullPolicy: IfNotPresent
        
        env:
        - name: GITHUB_TOKEN
          valueFrom:
            secretKeyRef:
              name: rmi-credentials
              key: github-token
        - name: OPENROUTER_API_KEY
          valueFrom:
            secretKeyRef:
              name: rmi-credentials
              key: openrouter-api-key
        - name: OPENROUTER_MODEL
          valueFrom:
            configMapKeyRef:
              name: rmi-config
              key: OPENROUTER_MODEL
        - name: LOG_LEVEL
          valueFrom:
            configMapKeyRef:
              name: rmi-config
              key: LOG_LEVEL
        - name: JAVA_OPTS
          valueFrom:
            configMapKeyRef:
              name: rmi-config
              key: JAVA_OPTS
        
        resources:
          limits:
            cpu: "1000m"
            memory: "512Mi"
          requests:
            cpu: "500m"
            memory: "256Mi"
        
        ports:
        - name: metrics
          containerPort: 8080
          protocol: TCP
        
        livenessProbe:
          httpGet:
            path: /health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        
        volumeMounts:
        - name: logs
          mountPath: /logs
        - name: cache
          mountPath: /cache
      
      volumes:
      - name: logs
        emptyDir: {}
      - name: cache
        emptyDir: {}
```

### Step 5: Create Service

**File: `k8s/service.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: rmi
  namespace: rmi
  labels:
    app: rmi
spec:
  type: ClusterIP
  ports:
  - name: metrics
    port: 8080
    targetPort: 8080
    protocol: TCP
  selector:
    app: rmi
```

### Step 6: Create ServiceAccount & RBAC

**File: `k8s/rbac.yaml`**

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: rmi
  namespace: rmi
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: rmi
  namespace: rmi
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: rmi
  namespace: rmi
subjects:
- kind: ServiceAccount
  name: rmi
  namespace: rmi
roleRef:
  kind: Role
  name: rmi
  apiGroup: rbac.authorization.k8s.io
```

### Step 7: Create HorizontalPodAutoscaler

**File: `k8s/hpa.yaml`**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: rmi
  namespace: rmi
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: rmi
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Step 8: Deploy to Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/rbac.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml

# Check deployment
kubectl get all -n rmi

# View pods
kubectl get pods -n rmi -w

# View logs
kubectl logs -f -n rmi deployment/rmi

# Check scaling
kubectl get hpa -n rmi
```

### Step 9: Install with Helm (Alternative)

**File: `helm/rmi/Chart.yaml`**

```yaml
apiVersion: v2
name: rmi
description: Repository Maintainability Index Helm Chart
type: application
version: 1.0.0
appVersion: "1.0.0"
keywords:
  - rmi
  - github
  - maintainability
maintainers:
  - name: Your Team
    email: team@example.com
```

**File: `helm/rmi/values.yaml`**

```yaml
replicaCount: 3

image:
  repository: your-registry.com/rmi-app
  tag: "1.0.0"
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 8080

resources:
  limits:
    cpu: 1000m
    memory: 512Mi
  requests:
    cpu: 500m
    memory: 256Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70

secrets:
  githubToken: ""
  openrouterApiKey: ""

config:
  openrouterModel: "openai/gpt-3.5-turbo"
  logLevel: "INFO"
```

**Deploy with Helm:**

```bash
# Install chart
helm install rmi ./helm/rmi \
  --namespace rmi \
  --create-namespace \
  --set secrets.githubToken="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" \
  --set secrets.openrouterApiKey="sk-or-xxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Upgrade
helm upgrade rmi ./helm/rmi --namespace rmi

# Rollback
helm rollback rmi --namespace rmi

# Uninstall
helm uninstall rmi --namespace rmi
```

---

## AWS Deployment

### Option 1: AWS ECS (Fargate)

**Step 1: Create Task Definition**

```json
{
  "family": "rmi",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::123456789012:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::123456789012:role/rmiTaskRole",
  "containerDefinitions": [
    {
      "name": "rmi",
      "image": "your-registry.com/rmi-app:1.0.0",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "OPENROUTER_MODEL",
          "value": "openai/gpt-3.5-turbo"
        },
        {
          "name": "LOG_LEVEL",
          "value": "INFO"
        }
      ],
      "secrets": [
        {
          "name": "GITHUB_TOKEN",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:rmi/github-token"
        },
        {
          "name": "OPENROUTER_API_KEY",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:rmi/openrouter-key"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/rmi",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "java -jar /app/rmi.jar --version || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3
      }
    }
  ]
}
```

**Step 2: Create Service**

```bash
# Create ECS service
aws ecs create-service \
  --cluster rmi-cluster \
  --service-name rmi \
  --task-definition rmi:1 \
  --desired-count 3 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-12345678,subnet-87654321],securityGroups=[sg-12345678],assignPublicIp=ENABLED}"
```

### Option 2: AWS EKS

```bash
# Create EKS cluster
eksctl create cluster \
  --name rmi-cluster \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 2 \
  --nodes-max 5

# Update kubeconfig
aws eks update-kubeconfig --name rmi-cluster --region us-east-1

# Deploy using kubectl (see Kubernetes section)
kubectl apply -f k8s/
```

---

## GCP Deployment

### Option 1: Google Cloud Run

```bash
# Build and push image to GCR
gcloud builds submit --tag gcr.io/your-project/rmi-app:1.0.0

# Deploy to Cloud Run
gcloud run deploy rmi \
  --image gcr.io/your-project/rmi-app:1.0.0 \
  --platform managed \
  --region us-central1 \
  --set-env-vars OPENROUTER_MODEL=openai/gpt-3.5-turbo,LOG_LEVEL=INFO \
  --set-secrets GITHUB_TOKEN=rmi-github-token:latest,OPENROUTER_API_KEY=rmi-openrouter-key:latest \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 2 \
  --max-instances 10 \
  --port 8080
```

### Option 2: Google Kubernetes Engine (GKE)

```bash
# Create GKE cluster
gcloud container clusters create rmi-cluster \
  --zone us-central1-a \
  --num-nodes 3 \
  --machine-type n1-standard-2 \
  --enable-autoscaling \
  --min-nodes 2 \
  --max-nodes 5

# Get credentials
gcloud container clusters get-credentials rmi-cluster --zone us-central1-a

# Deploy using kubectl (see Kubernetes section)
kubectl apply -f k8s/
```

---

## Azure Deployment

### Option 1: Azure Container Instances

```bash
# Create container instance
az container create \
  --resource-group rmi-rg \
  --name rmi \
  --image your-registry.azurecr.io/rmi-app:1.0.0 \
  --cpu 1 \
  --memory 1 \
  --environment-variables OPENROUTER_MODEL=openai/gpt-3.5-turbo LOG_LEVEL=INFO \
  --secure-environment-variables GITHUB_TOKEN=$GITHUB_TOKEN OPENROUTER_API_KEY=$OPENROUTER_API_KEY \
  --ports 8080 \
  --restart-policy Always
```

### Option 2: Azure Kubernetes Service (AKS)

```bash
# Create AKS cluster
az aks create \
  --resource-group rmi-rg \
  --name rmi-cluster \
  --node-count 3 \
  --node-vm-size Standard_D2_v2 \
  --enable-cluster-autoscaler \
  --min-count 2 \
  --max-count 5 \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group rmi-rg --name rmi-cluster

# Deploy using kubectl (see Kubernetes section)
kubectl apply -f k8s/
```

---

## Configuration Management

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `GITHUB_TOKEN` | ✅ Yes (recommended) | None | GitHub personal access token |
| `OPENROUTER_API_KEY` | ⚠️ If using LLM | None | OpenRouter API key |
| `OPENROUTER_MODEL` | No | `openai/gpt-3.5-turbo` | LLM model name |
| `LOG_LEVEL` | No | `INFO` | Logging level (DEBUG, INFO, WARN, ERROR) |
| `JAVA_OPTS` | No | Default JVM settings | JVM configuration options |

### Secrets Management

#### AWS Secrets Manager

```bash
# Store secret
aws secretsmanager create-secret \
  --name rmi/github-token \
  --secret-string "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Retrieve secret
aws secretsmanager get-secret-value --secret-id rmi/github-token --query SecretString --output text
```

#### GCP Secret Manager

```bash
# Store secret
echo -n "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" | \
  gcloud secrets create rmi-github-token --data-file=-

# Access secret
gcloud secrets versions access latest --secret="rmi-github-token"
```

#### Azure Key Vault

```bash
# Store secret
az keyvault secret set \
  --vault-name rmi-keyvault \
  --name github-token \
  --value "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Retrieve secret
az keyvault secret show --vault-name rmi-keyvault --name github-token --query value -o tsv
```

---

## Security Hardening

### 1. Network Security

```bash
# Restrict egress (firewall rules)
# Allow only:
# - api.github.com (443)
# - openrouter.ai (443)
# - Monitoring endpoints

# AWS Security Group
aws ec2 create-security-group \
  --group-name rmi-sg \
  --description "RMI security group"

aws ec2 authorize-security-group-egress \
  --group-id sg-12345678 \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0
```

### 2. IAM/RBAC Configuration

**AWS IAM Policy:**

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:us-east-1:123456789012:secret:rmi/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "*"
    }
  ]
}
```

### 3. Container Security

**Dockerfile Security Scan:**

```bash
# Trivy scan
trivy image your-registry.com/rmi-app:1.0.0

# Snyk scan
snyk container test your-registry.com/rmi-app:1.0.0
```

### 4. TLS/HTTPS Configuration

**Let's Encrypt with cert-manager (Kubernetes):**

```yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: rmi-tls
  namespace: rmi
spec:
  secretName: rmi-tls-cert
  issuerRef:
    name: letsencrypt-prod
    kind: ClusterIssuer
  dnsNames:
  - rmi.example.com
```

---

## Monitoring Setup

### Prometheus Configuration

**File: `prometheus-rmi.yml`**

```yaml
scrape_configs:
  - job_name: 'rmi'
    kubernetes_sd_configs:
    - role: pod
      namespaces:
        names:
        - rmi
    relabel_configs:
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
      action: keep
      regex: true
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_port]
      action: replace
      target_label: __address__
      regex: ([^:]+)(?::\d+)?;(\d+)
      replacement: $1:$2
```

---

## Rollback Procedures

### JAR Rollback

```bash
# Stop service
sudo systemctl stop rmi.service

# Restore previous version
sudo cp /opt/rmi/backup/repo-maintainability-index-0.9.0.jar /opt/rmi/bin/repo-maintainability-index-1.0.0.jar

# Start service
sudo systemctl start rmi.service
```

### Docker Rollback

```bash
# Rollback to previous image
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml pull rmi:0.9.0
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Rollback

```bash
# View deployment history
kubectl rollout history deployment/rmi -n rmi

# Rollback to previous version
kubectl rollout undo deployment/rmi -n rmi

# Rollback to specific revision
kubectl rollout undo deployment/rmi -n rmi --to-revision=2

# Check rollout status
kubectl rollout status deployment/rmi -n rmi
```

---

## Troubleshooting

### Common Issues

#### 1. Out of Memory

```bash
# Increase heap size
export JAVA_OPTS="-Xms512m -Xmx1024m"
```

#### 2. Connection Timeout

```bash
# Check network connectivity
curl -v https://api.github.com

# Verify firewall rules
# Allow egress to api.github.com:443
```

#### 3. Health Check Failures

```bash
# Check logs
kubectl logs -f deployment/rmi -n rmi

# Test health endpoint
curl http://localhost:8080/health/ready
```

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Next Review:** Quarterly  
**Owner:** DevOps/SRE Team
