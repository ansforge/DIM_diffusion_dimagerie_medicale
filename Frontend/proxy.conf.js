const PROXY_CONFIG = {
  "/api": {
    "target": "http://localhost:8081",
    "changeOrigin": true,
    "secure": false,
    "logLevel": "debug"
  },
  "/ohif": {
    "target": "http://localhost:8081",
    "changeOrigin": true,
    "secure": false,
    "logLevel": "debug"
  }
};
module.exports = PROXY_CONFIG;
