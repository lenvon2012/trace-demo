/* eslint-disable import/newline-after-import */
// initialize tracer
const express = require('express');
const CLSContext = require('zipkin-context-cls');
const {Tracer} = require('zipkin');
const {recorder} = require('./recorder');
const url = require('url');

const ctxImpl = new CLSContext('zipkin');
const localServiceName = 'docs-backend';
const tracer = new Tracer({ctxImpl, recorder, localServiceName});

const app = express();

// instrument the server
const zipkinMiddleware = require('zipkin-instrumentation-express').expressMiddleware;
app.use(zipkinMiddleware({tracer}));

app.get('/api', (req, res) => {
	var url_parts = url.parse(req.url, true);
	var query = url_parts.query;
	console.log(`docs-backend msg : ${query.msg}`);
	console.log(`docs-backend tracerId : ${tracer.id.traceId}, spanId : ${tracer.id.spanId}`);
	res.send(new Date().toString())
});

app.listen(9000, () => {
  console.log('Backend listening on port 9000!');
});
