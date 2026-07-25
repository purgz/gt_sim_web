# gt_sim_web
Web App for evolutionary simulator - see GTFyp


egt-platform/
├── backend/               ← Spring Boot
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── sim-service/           ← Python FastAPI  
│   ├── source/            ← your existing library
│   ├── main.py
│   ├── requirements.txt
│   └── Dockerfile
├── docker-compose.yml     ← local dev (all services)
├── docker-compose.prod.yml ← Oracle VM
└── .github/workflows/
    └── ci.yml


![alt text](image.png)