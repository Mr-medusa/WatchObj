<template>
    <div v-if="filterShow">
        <div v-if="propertyValue.path ==='CollectionBean#1730704097.rowList.rowList[1]'"></div>
        <span v-if="propertyValue.isShowKey">
            <span v-if="!isMap">{{ propertyValue.key }}</span>
            <span v-if="isMap">{{ prefixName }}</span>
            <span class="map-key-input-parent" tabindex="0" @mouseup="inputOnMouseupForMapKey" v-if="isMap">{{
                    collectionKey
                }}
                <input
                    ref='inputForMapKey'
                    v-show="isEditableForMapKey"
                    @blur="inputOnBlurForMapKey"
                    autofocus
                    v-model="collectionKey"
                    @input="propertyValueChangeForMapKey"
                    type="text"
                    class="map-key-input-child"/>
            </span>
            <span v-if="isMap">]</span>
            <span v-if="config.showPath">[{{ propertyValue.path }}-{{ propertyValue.type }}]</span>:
            <button @click="deletePropertyValue" class="delete-button" v-if="!this.propertyValue.isPrimitive">-</button>
        </span>
        <span v-if="propertyValue.isPrimitive" tabindex="0" @mouseup="inputOnMouseup" style="min-width: 30px"
              class="kv">
            {{ propertyValue.value }}
                <label>
                    <input ref='input'
                           v-show="isEditable"
                           v-model="propertyValue.value"
                           @blur="inputOnBlur"
                           @input="propertyValueChange"
                           autofocus
                           style="width: 100%;height: 100%;"
                           type="text"/>
                </label>
         </span>
        <button @click="deletePropertyValue" class="delete-button" v-if="this.propertyValue.isPrimitive">-</button>
        <div v-if="!propertyValue.isPrimitive">
            <span class="expand-json" v-show="isExpandAll" @click="isExpandAll=!isExpandAll">+</span>
            <span class="expand-json" v-show="!isExpandAll" @click="isExpandAll=!isExpandAll">-</span>
            <span v-show="!isExpandAll" v-if="showArrayPrefix" :style="arrayIntent">[</span>
            <span v-show="!isExpandAll" v-else :style="wrapIntent">{</span>
            <watch-property-value v-for="(item,key) in propertyValue.children"
                                  :key="key"
                                  :propertyValue="item"
                                  :style="valueIntent"
                                  v-show="!isExpandAll"
                                  :parent="propertyValue"
                                  @propertyValueChangeToParent="propertyValueChangeToParent"
            />
            <div :style="valueIntent" v-show="!isExpandAll">
                <button class="could-add-child-for-collection"
                        v-if="propertyValue.type === 'map' || propertyValue.type === 'collection'"
                        @click="addChildForCollection">+
                </button>
            </div>
            <span v-show="!isExpandAll" v-if="showArrayPrefix" :style="arrayIntent">]</span>
            <span v-show="!isExpandAll" v-else :style="wrapIntent">}</span>


        </div>
        <!--        <mutable-json v-if="!propertyValue.isPrimitive && propertyValue.isUnableControl"-->
        <!--                      :name="propertyValue.name"-->
        <!--                      :object="propertyValue.value"-->
        <!--                      :config="{showPath: false,isExpandAll:true,needParseJson: true,-->
        <!--                      jsonStyle:{color:'#2437ae',fontStyle:'italic'}}"/>-->

    </div>
</template>

<script>
import MutableJson from "@/components/json/MutableJson";
import _ from 'lodash'

export default {
    name: "WatchPropertyValue",
    props: ['propertyValue', 'parent'],
    inject: ['configToProvide'],
    components: {
        MutableJson
    },
    data() {
        return {
            config: this.configToProvide,
            // style
            isExpandAll: false,
            isEditable: false,
            isEditableForMapKey: false,
            showArrayPrefix: _.isArray(this.value),
            wrapIntent: {paddingLeft: '2em'},
            arrayIntent: {paddingLeft: '3em'},
            valueIntent: {},
            collectionKey: '',
            filterShow:true,
            // map 可以更新 键
            isMap: false,
            prefixName: '',
            watchTag: false,
        }
    },
    created() {
        this.valueIntent = {paddingLeft: this.showArrayPrefix ? '4em' : '3em'}
        this.isExpandAll = this.config.isExpandAll || false;
        if (this.parent && (this.propertyValue.type === 'map' || this.parent.type === 'map')) {
            let lastLRBraceIndex = this.lastLRBraceIndex();
            this.collectionKey = this.propertyValue.key.substring(lastLRBraceIndex[0] + 1, lastLRBraceIndex[1])
            this.prefixName = this.propertyValue.key.substring(0, lastLRBraceIndex[0] + 1)
        }

    },
    mounted() {
        let hasBracket = this.propertyValue.key.indexOf('[') !== -1
        this.isMap = hasBracket && (this.propertyValue.type === 'map') && !this.propertyValue.isWrapper
        if (this.$parent.propertyValue) {
            if (!this.isMap) {
                this.isMap = this.$parent.propertyValue.type === 'map' && !this.propertyValue.isWrapper;
            }
        }
    },
    methods: {
        // --- map-key
        inputOnMouseup() {
            this.isEditable = true;
            this.$nextTick(() => {
                this.$refs.input.focus();
            })
        },
        inputOnMouseupForMapKey() {
            this.isEditableForMapKey = true;
            this.$nextTick(() => {
                this.$refs.inputForMapKey.focus();
            })
        },
        // --- value
        inputOnBlur() {
            this.isEditable = false
        },
        inputOnBlurForMapKey() {
            this.isEditableForMapKey = false
        },
        propertyValueChangeToParent(data, callback) {
            this.$emit('propertyValueChangeToParent', data, callback)
        },
        propertyValueChange() {
            let message = {
                type: 'propertyValueChange', propertyValue: this.propertyValue
            }
            this.$emit('propertyValueChangeToParent', message)
        },
        propertyValueChangeForMapKey() {
            let message = {
                type: 'propertyValueChangeForMapKey',
                propertyValue: this.propertyValue,
                // parent: this.parent,
                newKey: this.prefixName + this.collectionKey + ']',
                newKeyIndex: this.collectionKey
            }

            this.$emit('propertyValueChangeToParent', message, () => {
                let mainKey = this.propertyValue.key.substring(0, this.propertyValue.key.lastIndexOf('['))
                let mainPath = this.propertyValue.path.substring(0, this.propertyValue.path.lastIndexOf('['))
                this.propertyValue.key = mainKey + '[' + this.collectionKey + ']'
                this.propertyValue.path = mainPath + '[' + this.collectionKey + ']'
            })

        },
        addChildForCollection() {

            let children = this.propertyValue.children;
            let newKeyIndex = children.length
            if (this.propertyValue.type === 'map') {
                newKeyIndex = 'key_' + children.length
            }
            let newPropertyValue = {
                isShowKey: true,
                key: this.propertyValue.key + '[' + newKeyIndex + ']',
                path: this.propertyValue.path + '.' + this.propertyValue.key + '[' + newKeyIndex + ']',
                isWrapper: false,
                isPrimitive: true,
                value: '',
                children: []
            };


            let message = {
                type: 'addChild',
                propertyValue: this.propertyValue,
                newKeyIndex: newKeyIndex,
                newPath: newPropertyValue.path,
                parent: this.parent
            }

            this.$emit('propertyValueChangeToParent', message)

            children.push(newPropertyValue)
        },
        deletePropertyValue() {
            let message = {
                type: 'deletePropertyValue', propertyValue: this.propertyValue, parent: this.parent
            }
            this.$emit('propertyValueChangeToParent', message)

            let index = 0;
            let children = this.parent.children;
            for (let i = 0; i < children.length; i++) {
                if (children[i].path === this.propertyValue.path) {
                    index = i
                    break
                }
            }
            children.splice(index, 1)
        },

        lastLRBraceIndex() {
            let lastLeftBrace = this.propertyValue.key.lastIndexOf('[')
            let lastRightBrace = this.propertyValue.key.lastIndexOf(']')
            return [lastLeftBrace, lastRightBrace]
        },
    },
    watch: {
        'propertyValue.key': function () {
            let lastLRBraceIndex = this.lastLRBraceIndex();
            this.collectionKey = this.propertyValue.key.substring(lastLRBraceIndex[0] + 1, lastLRBraceIndex[1])
            this.prefixName = this.propertyValue.key.substring(0, lastLRBraceIndex[0] + 1)
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

.map-key-input-parent {
    text-align: center;
    position: relative;
}

.map-key-input-child {
    border: none;
    font-size: 1em;
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
}

.map-key-input-child:focus {
    border-bottom: 1px gray;
}

.could-add-child-for-collection {
    width: 30px;
}

.could-add-child-for-collection:hover {
    cursor: pointer;
}

.delete-button:hover {
    cursor: pointer;
}

.delete-button {
    margin-left: 30px;
    border: none;
}

</style>