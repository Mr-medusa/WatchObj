(function(){"use strict";var e={545:function(e,t,r){r.r(t),r.d(t,{default:function(){return w}});var a=function(){var e=this,t=e._self._c;return t("div",[t("span",{directives:[{name:"show",rawName:"v-show",value:!e.isShown,expression:"!isShown"}],staticClass:"expand-json",on:{click:function(t){e.isShown=!e.isShown}}},[e._v("+")]),t("span",{directives:[{name:"show",rawName:"v-show",value:e.isShown,expression:"isShown"}],staticClass:"expand-json",on:{click:function(t){e.isShown=!e.isShown}}},[e._v("-")]),e.showArrayPrefix?t("span",{directives:[{name:"show",rawName:"v-show",value:e.isShown,expression:"isShown"}],style:e.arrayIntent},[e._v("[")]):t("span",{directives:[{name:"show",rawName:"v-show",value:e.isShown,expression:"isShown"}],style:e.wrapIntent},[e._v("{")]),e._l(e.propertyValues,(function(r,a){return t("div",{directives:[{name:"show",rawName:"v-show",value:e.isShown,expression:"isShown"}],key:a,style:e.valueIntent},[t("property-value",{ref:"child",refInFor:!0,attrs:{"property-value":r},on:{propertyValueChange:e.propertyValueChange}})],1)})),e.showArrayPrefix?t("span",{directives:[{name:"show",rawName:"v-show",value:e.isShown,expression:"isShown"}],style:e.arrayIntent},[e._v("]")]):t("span",{directives:[{name:"show",rawName:"v-show",value:e.isShown,expression:"isShown"}],style:e.wrapIntent},[e._v("}")])],2)},i=[],s=r(486),o=r.n(s),n=function(){var e=this,t=e._self._c;return t("div",[e.propertyValue.isShowKey||e.config.showPath?t("span",[e._v(e._s(e.propertyValue.key)+" "),e.config.showPath?t("span",[e._v("["+e._s(e.propertyValue.path)+"]")]):e._e(),e._v(": ")]):e._e(),e.isPrimitive?t("div",{ref:"div",staticClass:"kv",attrs:{tabindex:"0"},on:{mouseup:e.inputOnMouseup}},[e._v(e._s(e.propertyValue.value)+" "),t("label",[t("input",{directives:[{name:"show",rawName:"v-show",value:e.isEditable&&this.configToProvide.isEditable,expression:"isEditable && this.configToProvide.isEditable"},{name:"model",rawName:"v-model",value:e.propertyValue.value,expression:"propertyValue.value"}],ref:"input",staticStyle:{width:"100%",height:"100%"},attrs:{autofocus:"",type:"text"},domProps:{value:e.propertyValue.value},on:{blur:e.inputOnBlur,input:[function(t){t.target.composing||e.$set(e.propertyValue,"value",t.target.value)},e.propertyValueChange]}})])]):e._e(),e.isPrimitive?e._e():t("Json",{ref:"child",attrs:{json:e.json},on:{propertyValueChange:e.propertyValueChangePV}})],1)},l=[];const p=()=>Promise.resolve().then(r.bind(r,545));var u={name:"PropertyValue",props:["propertyValue"],inject:["configToProvide"],components:{Json:p},data(){return{config:this.configToProvide,isEditable:!1,json:{}}},created(){this.isPrimitive||(this.json={path:this.propertyValue.path,key:this.propertyValue.key,value:this.propertyValue.value,indent:this.propertyValue.indent+2})},mounted(){},computed:{isPrimitive:function(){let e=this.propertyValue.value;return o().isString(e)||o().isNumber(e)||o().isNil(e)||o().isBoolean(e)||o().isDate(e)}},methods:{inputOnMouseup(){this.isEditable=!0,this.$nextTick((()=>{this.$refs.input.focus()}))},inputOnBlur(){this.isEditable=!1},propertyValueChange(){this.$emit("propertyValueChange",this.propertyValue)},propertyValueChangePV(e){this.$emit("propertyValueChange",e)},updateProperty(e,t){return e===this.propertyValue.path?this.isPrimitive?(this.propertyValue.value=t,{code:"001",success:!0,msg:"操作成功"}):{code:"002",success:!1,msg:"只能修改原始类型..."}:((0,s.isNil)(this.$refs.child)||this.$refs.child.updateProperty(e,t),{code:"003",success:!1,msg:"正在修改中..."})}}},h=u,y=r(1),c=(0,y.Z)(h,n,l,!1,null,"f87684e4",null),d=c.exports,v={name:"json",props:["json","name"],inject:["configToProvide"],components:{PropertyValue:d},data(){return{config:this.configToProvide||{},isShown:!1,isEditable:!1,showArrayPrefix:o().isArray(this.json.value),wrapIntent:{paddingLeft:"2em"},arrayIntent:{paddingLeft:"3em"},valueIntent:{},propertyValues:[]}},created(){this.valueIntent={paddingLeft:this.showArrayPrefix?"4em":"3em"};let e=o().isNil(this.json.path)||""===this.json.path.trim();this.isShown=e||this.config.isExpandAll||this.isShown},mounted(){let e=o().isArray(this.json.value);this.config.needParseJson&&(this.json.value=JSON.parse(this.json.value));let t=o().keys(this.json.value);for(let r=0;r<t.length;r++){let a={path:o().isNil(this.json.path)?t[r]:this.json.path+(e?"[":".")+t[r]+(e?"]":""),key:t[r],value:this.json.value[t[r]],indent:this.json.indent,isShowKey:!this.showArrayPrefix};this.propertyValues.push(a)}},methods:{propertyValueChange(e){this.$emit("propertyValueChange",e)},updateProperty(e,t){let r=this.$refs.child;for(let a=0;a<r.length;a++){let i=r[a].updateProperty(e,t);if("001"===i.code||"002"===i.code)return i}}}},f=v,m=(0,y.Z)(f,a,i,!1,null,null,null),w=m.exports},164:function(e,t,r){var a=r(144),i=function(){var e=this,t=e._self._c;return t("div",{attrs:{id:"app"}},[t("watch-json")],1)},s=[],o=function(){var e=this,t=e._self._c;return t("div",{style:this.config.jsonStyle},[e.name?t("div",[e._v(e._s(e.name)+" :")]):e._e(),t("json",{ref:"el",attrs:{name:e.name,json:e.json,config:{showPath:!0}},on:{propertyValueChange:e.propertyValueChange}})],1)},n=[],l=r(545),p={name:"MutableJson",components:{json:l["default"]},provide(){return{configToProvide:this.config?this.config:{}}},props:["object","config","name"],data(){return{mutablePropertyValue:{key:"address.favorite[0]",value:"demo demo demo..."},json:{path:null,key:"value",value:this.object,indent:0}}},methods:{changeJsonValue(){this.$refs.el.updateProperty(this.mutablePropertyValue.key,this.mutablePropertyValue.value)},propertyValueChange(e){e.path===this.mutablePropertyValue.key&&(this.mutablePropertyValue.value=e.value)}}},u=p,h=r(1),y=(0,h.Z)(u,o,n,!1,null,"d792f34e",null),c=y.exports,d=function(){var e=this,t=e._self._c;return t("div",[!e.status&&e.tip?t("div",{staticStyle:{color:"red"}},[e._v(e._s(e.tip))]):e._e(),e.status&&e.tip?t("div",{staticStyle:{color:"green"}},[e._v(e._s(e.tip))]):e._e(),e._l(e.propertyValues,(function(r){return t("watch-property-value",{ref:"pvs",refInFor:!0,attrs:{"property-value":r,parent:null},on:{propertyValueChangeToParent:e.onPropertyValueChangeSlow}})})),t("div",{staticClass:"watch-obj-filter"},[t("input",{directives:[{name:"model",rawName:"v-model",value:e.watchObjFilter.key,expression:"watchObjFilter.key"}],attrs:{type:"text",placeholder:"key"},domProps:{value:e.watchObjFilter.key},on:{input:function(t){t.target.composing||e.$set(e.watchObjFilter,"key",t.target.value)}}}),e._v(" = "),t("input",{directives:[{name:"model",rawName:"v-model",value:e.watchObjFilter.value,expression:"watchObjFilter.value"}],attrs:{type:"text",placeholder:"value"},domProps:{value:e.watchObjFilter.value},on:{input:function(t){t.target.composing||e.$set(e.watchObjFilter,"value",t.target.value)}}})])],2)},v=[],f=function(){var e=this,t=e._self._c;return e.filterShow?t("div",["CollectionBean#1730704097.rowList.rowList[1]"===e.propertyValue.path?t("div"):e._e(),e.propertyValue.isShowKey?t("span",[e.isMap?e._e():t("span",[e._v(e._s(e.propertyValue.key))]),e.isMap?t("span",[e._v(e._s(e.prefixName))]):e._e(),e.isMap?t("span",{staticClass:"map-key-input-parent",attrs:{tabindex:"0"},on:{mouseup:e.inputOnMouseupForMapKey}},[e._v(e._s(e.collectionKey)+" "),t("input",{directives:[{name:"show",rawName:"v-show",value:e.isEditableForMapKey,expression:"isEditableForMapKey"},{name:"model",rawName:"v-model",value:e.collectionKey,expression:"collectionKey"}],ref:"inputForMapKey",staticClass:"map-key-input-child",attrs:{autofocus:"",type:"text"},domProps:{value:e.collectionKey},on:{blur:e.inputOnBlurForMapKey,input:[function(t){t.target.composing||(e.collectionKey=t.target.value)},e.propertyValueChangeForMapKey]}})]):e._e(),e.isMap?t("span",[e._v("]")]):e._e(),e.config.showPath?t("span",[e._v("["+e._s(e.propertyValue.path)+"-"+e._s(e.propertyValue.type)+"]")]):e._e(),e._v(": "),this.propertyValue.isPrimitive?e._e():t("button",{staticClass:"delete-button",on:{click:e.deletePropertyValue}},[e._v("-")])]):e._e(),e.propertyValue.isPrimitive?t("span",{staticClass:"kv",staticStyle:{"min-width":"30px"},attrs:{tabindex:"0"},on:{mouseup:e.inputOnMouseup}},[e._v(" "+e._s(e.propertyValue.value)+" "),t("label",[t("input",{directives:[{name:"show",rawName:"v-show",value:e.isEditable,expression:"isEditable"},{name:"model",rawName:"v-model",value:e.propertyValue.value,expression:"propertyValue.value"}],ref:"input",staticStyle:{width:"100%",height:"100%"},attrs:{autofocus:"",type:"text"},domProps:{value:e.propertyValue.value},on:{blur:e.inputOnBlur,input:[function(t){t.target.composing||e.$set(e.propertyValue,"value",t.target.value)},e.propertyValueChange]}})])]):e._e(),this.propertyValue.isPrimitive?t("button",{staticClass:"delete-button",on:{click:e.deletePropertyValue}},[e._v("-")]):e._e(),e.propertyValue.isPrimitive?e._e():t("div",[t("span",{directives:[{name:"show",rawName:"v-show",value:e.isExpandAll,expression:"isExpandAll"}],staticClass:"expand-json",on:{click:function(t){e.isExpandAll=!e.isExpandAll}}},[e._v("+")]),t("span",{directives:[{name:"show",rawName:"v-show",value:!e.isExpandAll,expression:"!isExpandAll"}],staticClass:"expand-json",on:{click:function(t){e.isExpandAll=!e.isExpandAll}}},[e._v("-")]),e.showArrayPrefix?t("span",{directives:[{name:"show",rawName:"v-show",value:!e.isExpandAll,expression:"!isExpandAll"}],style:e.arrayIntent},[e._v("[")]):t("span",{directives:[{name:"show",rawName:"v-show",value:!e.isExpandAll,expression:"!isExpandAll"}],style:e.wrapIntent},[e._v("{")]),e._l(e.propertyValue.children,(function(r,a){return t("watch-property-value",{directives:[{name:"show",rawName:"v-show",value:!e.isExpandAll,expression:"!isExpandAll"}],key:a,style:e.valueIntent,attrs:{propertyValue:r,parent:e.propertyValue},on:{propertyValueChangeToParent:e.propertyValueChangeToParent}})})),t("div",{directives:[{name:"show",rawName:"v-show",value:!e.isExpandAll,expression:"!isExpandAll"}],style:e.valueIntent},["map"===e.propertyValue.type||"collection"===e.propertyValue.type?t("button",{staticClass:"could-add-child-for-collection",on:{click:e.addChildForCollection}},[e._v("+ ")]):e._e()]),e.showArrayPrefix?t("span",{directives:[{name:"show",rawName:"v-show",value:!e.isExpandAll,expression:"!isExpandAll"}],style:e.arrayIntent},[e._v("]")]):t("span",{directives:[{name:"show",rawName:"v-show",value:!e.isExpandAll,expression:"!isExpandAll"}],style:e.wrapIntent},[e._v("}")])],2)]):e._e()},m=[],w=r(486),V=r.n(w),g={name:"WatchPropertyValue",props:["propertyValue","parent"],inject:["configToProvide"],components:{MutableJson:c},data(){return{config:this.configToProvide,isExpandAll:!1,isEditable:!1,isEditableForMapKey:!1,showArrayPrefix:V().isArray(this.value),wrapIntent:{paddingLeft:"2em"},arrayIntent:{paddingLeft:"3em"},valueIntent:{},collectionKey:"",filterShow:!0,isMap:!1,prefixName:"",watchTag:!1}},created(){if(this.valueIntent={paddingLeft:this.showArrayPrefix?"4em":"3em"},this.isExpandAll=this.config.isExpandAll||!1,this.parent&&("map"===this.propertyValue.type||"map"===this.parent.type)){let e=this.lastLRBraceIndex();this.collectionKey=this.propertyValue.key.substring(e[0]+1,e[1]),this.prefixName=this.propertyValue.key.substring(0,e[0]+1)}},mounted(){let e=-1!==this.propertyValue.key.indexOf("[");this.isMap=e&&"map"===this.propertyValue.type&&!this.propertyValue.isWrapper,this.$parent.propertyValue&&(this.isMap||(this.isMap="map"===this.$parent.propertyValue.type&&!this.propertyValue.isWrapper))},methods:{inputOnMouseup(){this.isEditable=!0,this.$nextTick((()=>{this.$refs.input.focus()}))},inputOnMouseupForMapKey(){this.isEditableForMapKey=!0,this.$nextTick((()=>{this.$refs.inputForMapKey.focus()}))},inputOnBlur(){this.isEditable=!1},inputOnBlurForMapKey(){this.isEditableForMapKey=!1},propertyValueChangeToParent(e,t){this.$emit("propertyValueChangeToParent",e,t)},propertyValueChange(){let e={type:"propertyValueChange",propertyValue:this.propertyValue};this.$emit("propertyValueChangeToParent",e)},propertyValueChangeForMapKey(){let e={type:"propertyValueChangeForMapKey",propertyValue:this.propertyValue,newKey:this.prefixName+this.collectionKey+"]",newKeyIndex:this.collectionKey};this.$emit("propertyValueChangeToParent",e,(()=>{let e=this.propertyValue.key.substring(0,this.propertyValue.key.lastIndexOf("[")),t=this.propertyValue.path.substring(0,this.propertyValue.path.lastIndexOf("["));this.propertyValue.key=e+"["+this.collectionKey+"]",this.propertyValue.path=t+"["+this.collectionKey+"]"}))},addChildForCollection(){let e=this.propertyValue.children,t=e.length;"map"===this.propertyValue.type&&(t="key_"+e.length);let r={isShowKey:!0,key:this.propertyValue.key+"["+t+"]",path:this.propertyValue.path+"."+this.propertyValue.key+"["+t+"]",isWrapper:!1,isPrimitive:!0,value:"",children:[]},a={type:"addChild",propertyValue:this.propertyValue,newKeyIndex:t,newPath:r.path,parent:this.parent};this.$emit("propertyValueChangeToParent",a),e.push(r)},deletePropertyValue(){let e={type:"deletePropertyValue",propertyValue:this.propertyValue,parent:this.parent};this.$emit("propertyValueChangeToParent",e);let t=0,r=this.parent.children;for(let a=0;a<r.length;a++)if(r[a].path===this.propertyValue.path){t=a;break}r.splice(t,1)},lastLRBraceIndex(){let e=this.propertyValue.key.lastIndexOf("["),t=this.propertyValue.key.lastIndexOf("]");return[e,t]}},watch:{"propertyValue.key":function(){let e=this.lastLRBraceIndex();this.collectionKey=this.propertyValue.key.substring(e[0]+1,e[1]),this.prefixName=this.propertyValue.key.substring(0,e[0]+1)}}},x=g,P=(0,h.Z)(x,f,m,!1,null,"4c73b3a2",null),b=P.exports,_={name:"WatchJson",components:{WatchPropertyValue:b},provide(){return{configToProvide:{showPath:!1,isExpandAll:!1,isEditable:!0}}},data(){return{watchObjFilter:{},mutablePropertyValue:{key:"hello",value:"demo demo demo..."},propertyValues:[],socket:null,status:null,tip:null}},created(){this.socket=window.WebSocket?new WebSocket("ws://localhost:9999/ws"):null},mounted(){null!=this.socket?(this.socket.onmessage=e=>{let t=e.data,r=JSON.parse(t),a=r.type,i=r.data,s=r.hint;if(this.tip=r.tip,this.status=r.success,!1!==this.status)if("addRoots"===a)this.propertyValues.push(...i);else if("propertyValueChange"===a)this.setPropertyValue(i.path,i.value);else if("propertyValueChangeForMapKey"===a){let e=this.getPropertyValue(s);e.key=i.key,e.path=i.path}else if("addChild"===a){let e=this.getPropertyValue(i.path);e.children.push(s)}else if("deletePropertyValue"===a)this.removePropertyValueFromParentChildren(i.path);else if("TIP"===a);else if("addRoot"===a)this.propertyValues.push(i);else if("removeRoot"===a){let e=-1;for(let t=0;t<this.propertyValues.length;t++)if(this.propertyValues[t].path===i.path){e=t;break}-1!==e&&this.propertyValues.splice(e,1)}},this.socket.onopen=e=>{console.log("连接开启..."),this.socket.send(1)},this.socket.onclose=function(e){console.log("连接关闭！")}):alert("浏览器不支持WebSocket")},methods:{onPropertyValueChangeSlow:V().debounce((function(e,t){let r={type:e.type,key:e.propertyValue.key,path:e.propertyValue.path,value:e.propertyValue.value,newKey:e.newKey,newKeyIndex:e.newKeyIndex,newPath:e.newPath};this.socketSend(JSON.stringify(r)),t&&t()}),1e3),changeJsonValue(){this.setPropertyValue(this.mutablePropertyValue.key,this.mutablePropertyValue.value)},setPropertyValue(e,t){for(let r=0;r<this.propertyValues.length;r++){let a=this.propertyValues[r],i=[a];i.push();while(0!==i.length){let r=i.pop().children;for(let a=0;a<r.length;a++){let s=r[a];if(s.path===e)return void(s.value=t);i.push(s)}}}},getPropertyValue(e){for(let t=0;t<this.propertyValues.length;t++){let r=this.propertyValues[t],a=[r];a.push();while(0!==a.length){let t=a.pop().children;for(let r=0;r<t.length;r++){let i=t[r];if(i.path===e)return i;a.push(i)}}}},removePropertyValueFromParentChildren(e){for(let t=0;t<this.propertyValues.length;t++){let r=this.propertyValues[t],a=[r];a.push();while(0!==a.length){let t=a.pop().children,r=0;for(let i=0;i<t.length;i++){let s=t[i];if(s.path===e){r=i;break}a.push(s)}t.splice(r,1)}}},socketSend(e){window.WebSocket&&(this.socket.readyState===WebSocket.OPEN?this.socket.send(e):alert("连接尚未开启"))},filterPropertyValue(e){if(this.watchObjFilter.key&&-1===e.propertyValue.path.indexOf(this.watchObjFilter.key)||this.watchObjFilter.value&&""+e.propertyValue.value!==this.watchObjFilter.value)e.filterShow=!1,e.$children.forEach((e=>{this.filterPropertyValue(e)}));else{let t=e;while(null!=t)t.filterShow=!0,t=t.$parent}}},watch:{watchObjFilter:{deep:!0,handler(){this.$refs.pvs[0].$children.forEach((e=>this.filterPropertyValue(e)))}}}},k=_,j=(0,h.Z)(k,d,v,!1,null,null,null),C=j.exports,S={name:"App",components:{json:c,watchJson:C},data(){return{object:{name:"zhangsan",age:180,address:{favorite:["apple","orange"]}}}}},O=S,E=(0,h.Z)(O,i,s,!1,null,null,null),A=E.exports;a.ZP.config.productionTip=!1,new a.ZP({render:function(e){return e(A)}}).$mount("#app")}},t={};function r(a){var i=t[a];if(void 0!==i)return i.exports;var s=t[a]={id:a,loaded:!1,exports:{}};return e[a].call(s.exports,s,s.exports,r),s.loaded=!0,s.exports}r.m=e,function(){var e=[];r.O=function(t,a,i,s){if(!a){var o=1/0;for(u=0;u<e.length;u++){a=e[u][0],i=e[u][1],s=e[u][2];for(var n=!0,l=0;l<a.length;l++)(!1&s||o>=s)&&Object.keys(r.O).every((function(e){return r.O[e](a[l])}))?a.splice(l--,1):(n=!1,s<o&&(o=s));if(n){e.splice(u--,1);var p=i();void 0!==p&&(t=p)}}return t}s=s||0;for(var u=e.length;u>0&&e[u-1][2]>s;u--)e[u]=e[u-1];e[u]=[a,i,s]}}(),function(){r.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return r.d(t,{a:t}),t}}(),function(){r.d=function(e,t){for(var a in t)r.o(t,a)&&!r.o(e,a)&&Object.defineProperty(e,a,{enumerable:!0,get:t[a]})}}(),function(){r.g=function(){if("object"===typeof globalThis)return globalThis;try{return this||new Function("return this")()}catch(e){if("object"===typeof window)return window}}()}(),function(){r.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)}}(),function(){r.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})}}(),function(){r.nmd=function(e){return e.paths=[],e.children||(e.children=[]),e}}(),function(){var e={143:0};r.O.j=function(t){return 0===e[t]};var t=function(t,a){var i,s,o=a[0],n=a[1],l=a[2],p=0;if(o.some((function(t){return 0!==e[t]}))){for(i in n)r.o(n,i)&&(r.m[i]=n[i]);if(l)var u=l(r)}for(t&&t(a);p<o.length;p++)s=o[p],r.o(e,s)&&e[s]&&e[s][0](),e[s]=0;return r.O(u)},a=self["webpackChunkwatch_me"]=self["webpackChunkwatch_me"]||[];a.forEach(t.bind(null,0)),a.push=t.bind(null,a.push.bind(a))}();var a=r.O(void 0,[998],(function(){return r(164)}));a=r.O(a)})();
//# sourceMappingURL=app.ed41c038.js.map