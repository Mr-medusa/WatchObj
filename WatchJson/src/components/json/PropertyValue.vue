<template>
    <div>
        <span v-if="propertyValue.isShowKey || config.showPath">{{ propertyValue.key }}
            <span v-if="config.showPath">[{{ propertyValue.path }}]</span>: </span>
        <div v-if="isPrimitive" tabindex="0" ref="div" @mouseup="inputOnMouseup" class="kv">{{ propertyValue.value }}
            <label>
                <input ref='input'
                       v-show="isEditable && this.configToProvide.isEditable"
                       v-model="propertyValue.value"
                       @blur="inputOnBlur"
                       @input="propertyValueChange"
                       autofocus
                       style="width: 100%;height: 100%;"
                       type="text"/>
            </label>
        </div>
        <Json ref=child v-if="!isPrimitive"
              :json="json"
              v-on:propertyValueChange="propertyValueChangePV"></Json>
    </div>
</template>

<script>
const Json = () => import('@/components/json/Json');
import _, {isNil} from 'lodash'

export default {
    name: "PropertyValue",
    props: ['propertyValue'],
    inject: ['configToProvide'],
    components: {
        Json
    },
    data() {
        return {
            config: this.configToProvide,
            isEditable: false,
            json: {},
        }
    },
    created() {
        if (!this.isPrimitive) {
            this.json = {
                path: this.propertyValue.path,
                key: this.propertyValue.key,
                value: this.propertyValue.value,
                indent: this.propertyValue.indent + 2,
            }
        }
    },
    mounted() {

    },
    computed: {
        isPrimitive: function () {
            let obj = this.propertyValue.value
            return _.isString(obj) || _.isNumber(obj) || _.isNil(obj)
                || _.isBoolean(obj) || _.isDate(obj);
        },
    },
    methods: {
        inputOnMouseup() {
            this.isEditable = true;
            this.$nextTick(() => {
                this.$refs.input.focus();
            })
        },
        inputOnBlur() {
            this.isEditable = false
        },
        propertyValueChange() {
            this.$emit('propertyValueChange', this.propertyValue)
        },
        propertyValueChangePV(v){
            this.$emit('propertyValueChange', v)
        },
        updateProperty(key, value) {
            if (key === this.propertyValue.path) {
                if (this.isPrimitive) {
                    this.propertyValue.value = value
                    return {
                        code: '001',
                        success: true,
                        msg: '操作成功'
                    }
                } else {
                    return {
                        code: '002',
                        success: false,
                        msg: '只能修改原始类型...'
                    }
                }
            } else {
                if (!isNil(this.$refs.child))
                    this.$refs.child.updateProperty(key, value)
                return {
                    code: '003',
                    success: false,
                    msg: '正在修改中...'
                }
            }
        },

    },

}
</script>

<style scoped>
.kv {
    height: 1em;
    display: inline-block;
    line-height: 1em;
    position: relative;
}

.kv input {
    border: none;
    position: absolute;
    left: 0;
    top: 0;
    padding: 0.3em 0.5em;
    font-weight: bold;
}

</style>