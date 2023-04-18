<template>
    <div :style="this.config.jsonStyle">
<!--                <hr/>-->
<!--                {{ mutablePropertyValue }}-->
        <div v-if="name">{{ name }} :</div>
        <json
            :name="name"
            :json="json"
            ref="el"
            v-on:propertyValueChange="propertyValueChange"
            :config="{showPath:true}"
        />

<!--                <input type="text" v-model="mutablePr((TypeVariableImpl) A.class.getSuperclass().getDeclaredField("a1").getGenericType()).getBounds()opertyValue.key"/>-->
<!--                <input type="text" v-model="mutablePropertyValue.value"/>-->
<!--                <button @click="changeJsonValue">修改</button>-->
    </div>
</template>

<script>
import json from "@/components/json/Json";

export default {
    name: "MutableJson",
    components: {
        json
    },
    provide() {
        return {
            configToProvide: this.config ? this.config : {}
        }
    },
    props: ['object', 'config', 'name'],
    data() {
        return {
            mutablePropertyValue: {
                key: 'address.favorite[0]',
                value: 'demo demo demo...',
            },
            json: {
                path: null,
                key: 'value',
                value: this.object,
                indent: 0
            }
        }
    },
    methods: {
        changeJsonValue() {
            this.$refs.el.updateProperty(this.mutablePropertyValue.key, this.mutablePropertyValue.value)
        },
        propertyValueChange(v) {
            if (v.path === this.mutablePropertyValue.key) {
                this.mutablePropertyValue.value = v.value
            }
        }
    }
}
</script>

<style scoped>

</style>