let dir = "G:\\HWorkspace\\WatchObj\\src\\main\\resources\\dist"
module.exports = {
    outputDir: dir,
    configureWebpack: {
        output: {
            // 输出重构  打包编译后的 文件名称  【模块名称.版本号】
            filename: `js2/[name].[contenthash:8].js`,
            chunkFilename: `js2/[name].[contenthash:8].js`
        },
    },
    // 对本地服务器进行配置
    devServer: {
        proxy: {
            //以/netRequest作为开头的axios请求都会进行代理
            "/netRequest": {
                target: "http://localhost:8888", //请求目标服务器的url
                changeOrigin: false, //是否对服务器隐藏源地址（选为true）
                pathRewrite: {
                    "^/netRequest": ""    //将"/netRequest"前缀的路径用""替换
                }
            }
        },
        port: 9090,  //vue前端的端口号
    },
}