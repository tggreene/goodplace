const path = require("path");

module.exports = {
  mode: "production",
  entry: "./target/index.js",
  output: {
    path: path.resolve(__dirname, "target/webpack"),
    filename: "libs.js"
  }
};
