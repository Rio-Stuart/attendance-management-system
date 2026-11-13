// netlify/functions/api.mjs — Netlify Functions v2 handler
import { createRequire } from 'module';
const require = createRequire(import.meta.url);
const serverless = require('serverless-http');
const app = require('../../server.js');

const handler = serverless(app);

export default async (req, context) => {
  const url = new URL(req.url);
  const headers = {};
  for (const [k, v] of req.headers.entries()) {
    headers[k] = v;
  }

  const hasBody = req.method !== 'GET' && req.method !== 'HEAD';
  const bodyText = hasBody ? await req.text() : null;

  const event = {
    httpMethod: req.method,
    path: url.pathname,
    queryStringParameters: Object.fromEntries(url.searchParams.entries()),
    headers,
    body: bodyText,
    isBase64Encoded: false
  };

  const lambdaRes = await handler(event, context || {});

  const resHeaders = new Headers();
  if (lambdaRes.headers) {
    for (const [k, v] of Object.entries(lambdaRes.headers)) {
      if (v !== undefined && v !== null) {
        resHeaders.set(k, String(v));
      }
    }
  }

  return new Response(lambdaRes.body, {
    status: lambdaRes.statusCode || 200,
    headers: resHeaders
  });
};
