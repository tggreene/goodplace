const path = require("path");

module.exports = {
  mode: "production",
  entry: "./target/index.js",
  output: {
    path: path.resolve(__dirname, "target/webpack"),
    // path: path.resolve(__dirname, "public-prod/js"),
    filename: "libs.js"
  }
};
