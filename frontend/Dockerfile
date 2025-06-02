FROM node:lts-alpine3.20 AS build

COPY . /frontend
WORKDIR /frontend

RUN npm ci && npm run build

FROM nginx:1.28.0-alpine-slim AS final

COPY --from=build /frontend/dist /var/www/
COPY ./entrypoint.sh /
RUN chmod +x /entrypoint.sh
RUN apk add --no-cache gettext

CMD ["/entrypoint.sh", "nginx", "-g", "daemon off;"]
