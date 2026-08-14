FROM node:20-alpine AS builder
WORKDIR /workspace
RUN corepack enable
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/parking-admin/package.json apps/parking-admin/package.json
COPY apps/parking-uniapp/package.json apps/parking-uniapp/package.json
RUN pnpm install --frozen-lockfile --filter @neighbor-parking/admin...
COPY apps/parking-admin apps/parking-admin
ARG VITE_API_BASE_URL=http://localhost:8080/api/v1
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN pnpm --dir apps/parking-admin build

FROM nginx:1.27-alpine
COPY deploy/nginx-spa.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /workspace/apps/parking-admin/dist /usr/share/nginx/html
EXPOSE 80
