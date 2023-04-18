<template>
    <div>

        <span class="expand-json" v-show="!isShown" @click="isShown=!isShown">+</span>
        <span class="expand-json" v-show="isShown" @click="isShown=!isShown">-</span>
        <span v-show="isShown" v-if="showArrayPrefix" :style="arrayIntent">[</span>
        <span v-show="isShown" v-else :style="wrapIntent">{</span>
        <div v-show="isShown" v-for="(item,index) in propertyValues " :key="index" :style="valueIntent">
            <property-value ref="child"
                            :property-value="item"
                            v-on:propertyValueChange="propertyValueChange"/>
        </div>
        <span v-show="isShown" v-if="showArrayPrefix" :style="arrayIntent">]</span>
        <span v-show="isShown" v-else :style="wrapIntent">}</span>
    </div>
</template>

<script>
import _ from 'lodash';
import PropertyValue from './PropertyValue'

export default {
    name: "json",
    props: ['json','name'],
    inject: ['configToProvide'],
    components: {
        PropertyValue
    },
    data() {
        return {
            config: this.configToProvide || {},
            // style
            isShown: false,
            isEditable: false,
            showArrayPrefix: _.isArray(this.json.value),
            wrapIntent: {paddingLeft: '2em'},
            arrayIntent: {paddingLeft: '3em'},
            valueIntent: {},
            // model
            propertyValues: []
        }
    },
    created() {
        this.valueIntent = {paddingLeft: this.showArrayPrefix ? '4em' : '3em'}
        let isRoot = _.isNil(this.json.path) || this.json.path.trim() === '';
        this.isShown = isRoot || this.config.isExpandAll || this.isShown;
    },
    mounted() {
        let isArray = _.isArray(this.json.value)
        if(this.config.needParseJson){
            this.json.value = JSON.parse(this.json.value);
        }
        let keys = _.keys(this.json.value);
        for (let i = 0; i < keys.length; i++) {
            let v = {
                path: _.isNil(this.json.path) ? keys[i] : (this.json.path + (isArray ? '[' : '.') + keys[i] + (isArray ? ']' : '')),
                key: keys[i],
                value: this.json.value[keys[i]],
                indent: this.json.indent,
                isShowKey: !this.showArrayPrefix
            }
            this.propertyValues.push(v)
        }
    },
    methods: {
        propertyValueChange(v) {
            this.$emit('propertyValueChange', v)
        },
        updateProperty(key, value) {
            let children = this.$refs.child
            for (let i = 0; i < children.length; i++) {
                let result = children[i].updateProperty(key, value);
                if (result.code === '001' || result.code === '002') {
                    return result;
                }
            }
        }
    },

}
</script>
<style>
.expand-json:hover {
    cursor: pointer;
}
</style>