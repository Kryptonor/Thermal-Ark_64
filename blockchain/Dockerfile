# Blockchain Dockerfile
FROM node:18-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci --only=production

# Copy source code
COPY . .

# Install truffle globally
RUN npm install -g truffle

EXPOSE 8545

CMD ["truffle", "develop"]