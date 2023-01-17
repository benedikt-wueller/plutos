import { createApp } from 'vue/dist/vue.esm-bundler'
import { createRouter, createWebHashHistory } from 'vue-router'

import store from './store.js'
import './style.css'

import VueTailwindDatepicker from 'vue-tailwind-datepicker'

import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { far } from '@fortawesome/free-regular-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(fas, far)

import App from './App.vue'
import Home from "./views/Home.vue";
import Categories from "./views/Categories.vue";
import Category from "./views/Category.vue";
import Statements from "./views/Statements.vue";
import Accounts from "./views/Accounts.vue";
import Tags from "./views/Tags.vue";
import Tag from "./views/Tag.vue";

const routes = [
    { path: '/', component: Home },
    { path: '/statements', component: Statements },
    { path: '/accounts', component: Accounts },
    { path: '/categories', component: Categories },
    { path: '/categories/:categoryId', component: Category },
    { path: '/tags', component: Tags },
    { path: '/tags/:tagId', component: Tag },
]

const router = createRouter({
    history: createWebHashHistory(),
    routes,
})

const app = createApp(App)
app.use(router)
app.use(VueTailwindDatepicker)
app.use(store)

app.component('font-awesome-icon', FontAwesomeIcon)

app.config.globalProperties.$filters = {
    formatNumber(number, digits = 2) {
        return parseFloat(number).toFixed(digits)
    }
}

app.mount('#app')
