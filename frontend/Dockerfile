FROM node:lts-alpine3.20

WORKDIR /frontend
COPY . /frontend

RUN npm i

CMD [ "npm", "run", "dev", "--", "--host" ]
