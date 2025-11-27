# IoT Simulator Dockerfile
FROM node:18-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci --only=production

# Copy source code
COPY . .

EXPOSE 3001

CMD ["npm", "start"]