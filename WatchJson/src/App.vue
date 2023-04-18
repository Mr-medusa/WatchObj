<template>
    <div id="app">
<!--        <json :object="object" :config="{showPath: false,isExpandAll:true}"/>-->
        <watch-json/>
    </div>
</template>

<script>
import json from "@/components/json/MutableJson";
import watchJson from "@/components/WatchJson";

export default {
    name: 'App',
    components: {
        json,
        watchJson
    },
    data() {
        return {
            object: {
                name: 'zhangsan',
                age: 180,
                address: {
                    favorite: ['apple', 'orange']
                },
            }
        }
    },
}

</script>

<style>
#app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #2c3e50;

    text-align: left;
    vertical-align: baseline;
}

#app input {

}
</style>
