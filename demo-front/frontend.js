/* eslint-disable import/newline-after-import */
// initialize tracer
const rest = require('rest');
const express = require('express');
const CLSContext = require('zipkin-context-cls');
const {Tracer} = require('zipkin');
const {recorder} = require('./recorder');
const url = require('url');

const ctxImpl = new CLSContext('zipkin');
const localServiceName = 'docs-front';
const tracer = new Tracer({ctxImpl, recorder, localServiceName});

const app = express();

// instrument the server
const zipkinMiddleware = require('zipkin-instrumentation-express').expressMiddleware;
app.use(zipkinMiddleware({tracer}));

// instrument the client
const {restInterceptor} = require('zipkin-instrumentation-cujojs-rest');
const zipkinRest = rest.wrap(restInterceptor, {tracer});

// Allow cross-origin, traced requests. See http://enable-cors.org/server_expressjs.html
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', [
    'Origin', 'Accept', 'X-Requested-With', 'X-B3-TraceId',
    'X-B3-ParentSpanId', 'X-B3-SpanId', 'X-B3-Sampled'
  ].join(', '));
  next();
});

app.get('/redis', (req, res) => {
  var url_parts = url.parse(req.url, true);
  var query = url_parts.query;
  res.header('X-B3-TraceId', tracer.id.traceId);
  res.header('X-B3-SpanId', tracer.id.spanId);
  tracer.local('docs-front', () =>
    zipkinRest(`http://localhost:8080/demo/web/callredis?msg=${query.msg}`)
      .then(response => res.send(response.entity))
      .catch(err => console.error('Error', err.stack))
  );
  console.log(`tracerId : ${tracer.id.traceId}, spanId : ${tracer.id.spanId}`);
});

app.get('/kafka', (req, res) => {
	  var url_parts = url.parse(req.url, true);
	  var query = url_parts.query;
	  res.header('X-B3-TraceId', tracer.id.traceId);
	  res.header('X-B3-SpanId', tracer.id.spanId);
	  tracer.local('docs-front', () =>
	    zipkinRest(`http://localhost:8080/demo/web/callkafka?msg=${query.msg}`)
	      .then(response => res.send(response.entity))
	      .catch(err => console.error('Error', err.stack))
	  );
	  console.log(`tracerId : ${tracer.id.traceId}, spanId : ${tracer.id.spanId}`);
	});

app.listen(8081, () => {
  console.log('Frontend listening on port 8081!');
});
