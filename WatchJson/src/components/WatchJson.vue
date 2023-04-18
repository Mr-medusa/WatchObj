<template>
    <div>
        <div style="color: red" v-if="!status && tip">{{ tip }}</div>
        <div style="color: green" v-if="status && tip">{{ tip }}</div>
        <watch-property-value ref="pvs" v-for="item in propertyValues"
                              :property-value="item"
                              :parent="null"
                              v-on:propertyValueChangeToParent="onPropertyValueChangeSlow"/>
        <!--        {{ mutablePropertyValue }}-->
        <!--        <input type="text" v-model="mutablePropertyValue.path"/>-->
        <!--        <input type="text" v-model="mutablePropertyValue.value"/>-->
        <!--        <button @click="changeJsonValue">修改</button>-->
        <div class="watch-obj-filter">
            <input type="text" v-model="watchObjFilter.key" placeholder="key"/> =
            <input type="text" v-model="watchObjFilter.value" placeholder="value"/>
        </div>
    </div>
</template>

<script>
import WatchPropertyValue from './WatchPropertyValue'
import _ from 'lodash'

export default {
    name: "WatchJson",
    components: {
        WatchPropertyValue
    },
    provide() {
        return {
            configToProvide: {
                showPath: false,
                isExpandAll: false,
                isEditable: true,
            },

        }
    },
    data() {
        return {
            watchObjFilter: {},
            mutablePropertyValue: {
                key: 'hello',
                value: 'demo demo demo...',
            },
            // model
            propertyValues: [],
            socket: null,
            status: null,
            tip: null,
        }
    },
    created() {
        this.socket = window.WebSocket ? new WebSocket("ws://localhost:9999/ws") : null;
        // let root = {
        //     isShowKey: true,
        //     isUnableControl: true,
        //     isPrimitive: false,
        //     path: '123#key',
        //     key: "123#key",
        //     value: "value",
        //     root: null
        // }
        // root.children = [
        //     {
        //         isShowKey: true,
        //         isUnableControl: true,
        //         isPrimitive: true,
        //         path: '123#key.hello',
        //         key: "hello",
        //         value: "value",
        //         parent: root
        //     }
        // ]
        //
        // this.propertyValues.push(root)
    },
    mounted() {
        if (this.socket != null) {
            this.socket.onmessage = (event) => {
                let message = event.data
                let result = JSON.parse(message)
                let type = result.type
                let responseData = result.data
                let hint = result.hint
                this.tip = result.tip;
                this.status = result.success;
                if (this.status === false) {
                    return;
                }
                // setTimeout(() => {
                //     this.tip = null;
                // }, 5000)
                if (type === 'addRoots') {
                    this.propertyValues.push(...responseData)
                } else if (type === 'propertyValueChange') {
                    this.setPropertyValue(responseData.path, responseData.value)
                } else if (type === 'propertyValueChangeForMapKey') {
                    let oldPropertyValue = this.getPropertyValue(hint);
                    oldPropertyValue.key = responseData.key;
                    oldPropertyValue.path = responseData.path;
                } else if (type === 'addChild') {
                    let parentPropertyValue = this.getPropertyValue(responseData.path);
                    parentPropertyValue.children.push(hint)
                } else if (type === 'deletePropertyValue') {
                    this.removePropertyValueFromParentChildren(responseData.path)
                } else if (type === 'TIP') {

                } else if (type === 'addRoot') {
                    this.propertyValues.push(responseData)
                } else if (type === 'removeRoot') {
                    let index = -1;
                    for (let j = 0; j < this.propertyValues.length; j++) {
                        if (this.propertyValues[j].path === responseData.path) {
                            index = j;
                            break;
                        }
                    }
                    if (index !== -1) {
                        this.propertyValues.splice(index, 1)
                    }
                }
            }
            this.socket.onopen = event => {
                console.log("连接开启...")
                this.socket.send(1);
            }
            this.socket.onclose = function (event) {
                console.log("连接关闭！")
            }

        } else {
            alert("浏览器不支持WebSocket")
        }
    },
    methods: {
        onPropertyValueChangeSlow: _.debounce(function (val, callback) {
            let message = {
                type: val.type,
                key: val.propertyValue.key,
                path: val.propertyValue.path,
                value: val.propertyValue.value,
                // update key
                newKey: val.newKey,
                // add
                newKeyIndex: val.newKeyIndex,
                newPath: val.newPath
            }
            this.socketSend(JSON.stringify(message));

            if (callback) {
                callback()
            }
        }, 1000),
        changeJsonValue() {
            this.setPropertyValue(this.mutablePropertyValue.key, this.mutablePropertyValue.value)
        },
        setPropertyValue(name, value) {
            for (let j = 0; j < this.propertyValues.length; j++) {
                let node = this.propertyValues[j];
                let stack = [node];
                stack.push();
                while (stack.length !== 0) {
                    let children = stack.pop().children;
                    for (let i = 0; i < children.length; i++) {
                        let child = children[i];
                        if (child.path === name) {
                            child.value = value;
                            return;
                        }
                        stack.push(child);
                    }
                }
            }

        },
        getPropertyValue(name) {
            for (let j = 0; j < this.propertyValues.length; j++) {
                let node = this.propertyValues[j];
                let stack = [node];
                stack.push();
                while (stack.length !== 0) {
                    let children = stack.pop().children;
                    for (let i = 0; i < children.length; i++) {
                        let child = children[i];
                        if (child.path === name) {
                            return child;
                        }
                        stack.push(child);
                    }
                }
            }
        },
        removePropertyValueFromParentChildren(name) {
            for (let j = 0; j < this.propertyValues.length; j++) {
                let node = this.propertyValues[j];
                let stack = [node];
                stack.push();
                while (stack.length !== 0) {
                    let children = stack.pop().children;
                    let index = 0;
                    for (let i = 0; i < children.length; i++) {
                        let child = children[i];
                        if (child.path === name) {
                            index = i;
                            break;
                        }
                        stack.push(child);
                    }
                    children.splice(index, 1)
                }
            }
        },
        socketSend(message) {
            if (!window.WebSocket) {
                return;
            }
            if (this.socket.readyState === WebSocket.OPEN) {
                this.socket.send(message);
            } else {
                alert("连接尚未开启");
            }
        },
        filterPropertyValue(pvVue) {
            if (
                (!this.watchObjFilter.key || pvVue.propertyValue.path.indexOf(this.watchObjFilter.key) !== -1) &&
                (!this.watchObjFilter.value ||  ''+pvVue.propertyValue.value === this.watchObjFilter.value)
            ) {

                let that = pvVue
                while (that != null) {
                    that.filterShow = true
                    that = that.$parent;
                }
            } else {
                pvVue.filterShow = false
                pvVue.$children.forEach(it => {
                    this.filterPropertyValue(it)
                })
            }
        }
    },
    watch: {
        watchObjFilter: {
            deep: true,
            handler() {
                this.$refs.pvs[0].$children.forEach(it => this.filterPropertyValue(it))
            }
        }
    }
}
</script>
<style>
html, body {
    font-size: 20px;
}
.watch-obj-filter{
    position: fixed;
    bottom: 0;
    left: 0;
    border-top: 2px solid #ccc;
}
</style>