const path = require("path");

module.exports = {
  mode: "development",
  entry: "./target/index.js",
  devtool: "source-map",
  output: {
    path: path.resolve(__dirname, "public/js/"),
    filename: "libs.js"
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        enforce: "pre",
        use: ["source-map-loader"],
      },
    ],
  },
};
