const path = require("path");
const systemConfig = require("./src/lib/system.config");
const MiniCssExtractPlugin = require("mini-css-extract-plugin"); //表示自定义的系统配置
module.exports = {
    // 基本路径
    publicPath: "/",
    // 输出文件目录
    outputDir: path.resolve(__dirname, "../server/client"),
    // eslint-loader 是否在保存的时候检查
    lintOnSave: true,
    configureWebpack: {
        output: {
            // 输出重构  打包编译后的 文件名称  【模块名称.版本号】
            filename: `js/[name].${systemConfig.version}.js`,
            chunkFilename: `js/[name].${systemConfig.version}.js`
        },
        plugins: [
            new MiniCssExtractPlugin({
                // 修改打包后css文件名
                filename: `css/[name].${systemConfig.version}.css`,
                chunkFilename: `css/[name].${systemConfig.version}.css`
            })
        ]
    },
    // 修改打包后img文件名
    chainWebpack: config => {
        config.module
            .rule("images")
            .use("url-loader")
            .tap(options => {
                options.name = `img/[name].${systemConfig.version}.[ext]`;
                options.fallback = {
                    loader: "file-loader",
                    options: {
                        name: `img/[name].${systemConfig.version}.[ext]`
                    }
                };
                return options;
            });
    },
    // webpack-dev-server 相关配置
    devServer: {
        open: true,
        host: "localhost",
        port: 8080,
        https: false,
        hotOnly: false,
        proxy: {
            "/api/app": {
                target: "http://localhost:5423",
                changeOrigin: true, //是否跨域
                pathRewrite: {
                    "^/api/app": "/api/app" //重写接口
                }
            }
        }
    }
};