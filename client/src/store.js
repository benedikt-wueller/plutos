import { createStore } from 'vuex'
import axios from "axios";
import dayjs from "dayjs";

const store = {
    state() {
        return {
            // Basics
            baseUrl: 'http://localhost:8143/api/v1',

            // Filters
            statementFilters: {
                range: {
                    from: dayjs().startOf("month").format('YYYY-MM-DD'),
                    to: dayjs().endOf("month").format('YYYY-MM-DD')
                },
                accounts: [],
                categories: [],
                tags: [],
            },

            // Util
            loading: 0,
            importers: [],
            toasts: [],

            // Entities
            categories: {},
            categoryPatterns: {},
            tags: {},
            tagPatterns: {},
            statements: {},
            accounts: {},
        }
    },
    mutations: {
        initializeStatementFilters(state) {
            const filters = localStorage.getItem('statementFilters')
            if (!filters) return
            state.statementFilters = JSON.parse(filters)
        },

        setStatementFilters(state, payload) {
            state.statementFilters = {
                range: payload.range,
                accounts: payload.accounts,
                categories: payload.categories,
                tags: payload.tags
            }
            localStorage.setItem('statementFilters', JSON.stringify(state.statementFilters))
        },

        incrementLoading(state) {
            state.loading += 1
        },
        decrementLoading(state) {
            state.loading -= 1
        },

        pushStatements(state, payload) {
            payload.entities.forEach(entity => state.statements[entity.id] = entity)
        },
        removeStatement(state, payload) {
            delete state.statements[payload.id]
        },
        clearStatements(state) {
            state.statements = {}
        },

        pushCategories(state, payload) {
            payload.entities.forEach(entity => state.categories[entity.id] = entity)
        },
        removeCategory(state, payload) {
            delete state.categories[payload.id]
        },
        clearCategories(state) {
            state.categories = {}
        },

        pushCategoryPatterns(state, payload) {
            payload.entities.forEach(entity => state.categoryPatterns[entity.id] = entity)
        },
        removeCategoryPattern(state, payload) {
            delete state.categoryPatterns[payload.id]
        },
        clearCategoryPatterns(state) {
            state.categoryPatterns = {}
        },

        pushAccounts(state, payload) {
            payload.entities.forEach(entity => state.accounts[entity.id] = entity)
        },
        removeAccount(state, payload) {
            delete state.accounts[payload.id]
        },
        clearAccounts(state) {
            state.accounts = {}
        },

        pushTags(state, payload) {
            payload.entities.forEach(entity => state.tags[entity.id] = entity)
        },
        removeTag(state, payload) {
            delete state.tags[payload.id]
        },
        clearTags(state) {
            state.tags = {}
        },

        pushTagPatterns(state, payload) {
            payload.entities.forEach(entity => state.tagPatterns[entity.id] = entity)
        },
        removeTagPattern(state, payload) {
            delete state.tagPatterns[payload.id]
        },
        clearTagPatterns(state) {
            state.tagPatterns = {}
        },

        setImporters(state, payload) {
            state.importers = payload.importers
        },

        addToast(state, payload) {
            state.toasts = [...state.toasts, payload]
        },
        removeToast(state, payload) {
            state.toasts = state.toasts.filter((_, i) => i !== payload.index)
        },
        clearToast(state, payload) {
            state.toasts = {}
        },
    },
    actions: {
        fetchStatements(context, payload) {
            context.commit('incrementLoading')
            return axios.get(context.state.baseUrl + '/statements', { params: { from: payload.from, to: payload.to } }).then(response => {
                context.commit('pushStatements', { entities: response.data.data })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        createStatement(context, payload) {
            payload.attributes.currency = payload.attributes.currency.toUpperCase()

            context.commit('incrementLoading')
            return axios.post(context.state.baseUrl + '/statements', {
                data: {
                    type: 'statements',
                    attributes: payload.attributes,
                    relationships: payload.relationships
                }
            }).then(response => {
                context.commit('decrementLoading')
                context.commit('pushStatements', { entities: [response.data.data] })
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        updateStatement(context, payload) {
            payload.attributes.currency = payload.attributes.currency.toUpperCase()

            context.commit('incrementLoading')
            return axios.patch(context.state.baseUrl + '/statements/' + payload.id, {
                data: {
                    type: 'statements',
                    id: payload.id,
                    attributes: payload.attributes,
                    relationships: payload.relationships,
                }
            }).then(response => {
                context.commit('pushStatements', { entities: [response.data.data] })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        deleteStatement(context, payload) {
            context.commit('incrementLoading')
            return axios.delete(context.state.baseUrl + '/statements/' + payload.id).then(() => {
                context.commit('removeStatement', payload)
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        clearStatements(context) {
            context.commit('incrementLoading')
            return axios.delete(context.state.baseUrl + '/statements').then(() => {
                context.commit('clearStatements')
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },

        fetchCategories(context) {
            context.commit('incrementLoading')
            context.commit('clearCategories')
            return axios.get(context.state.baseUrl + '/categories').then(response => {
                context.commit('pushCategories', { entities: response.data.data })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        loadCategory(context, payload) {
            if (context.getters.findCategory(payload.id)) return
            context.commit('incrementLoading')
            return axios.get(context.state.baseUrl + '/categories/' + payload.id).then(response => {
                context.commit('pushCategories', { entities: [response.data.data] })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        createCategory(context, payload) {
            context.commit('incrementLoading')
            return axios.post(context.state.baseUrl + '/categories', {
                data: {
                    type: 'categories',
                    attributes: payload.attributes
                }
            }).then(response => {
                context.commit('decrementLoading')
                if (!response.data.data.attributes.default) {
                    context.commit('pushCategories', { entities: [response.data.data] })
                    return
                }
                return context.dispatch('fetchCategories')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        updateCategory(context, payload) {
            payload.attributes.limit = payload.attributes.limit || null

            context.commit('incrementLoading')
            return axios.patch(context.state.baseUrl + '/categories/' + payload.id, {
                data: {
                    type: 'categories',
                    id: payload.id,
                    attributes: payload.attributes
                }
            }).then(response => {
                context.commit('decrementLoading')
                if (!response.data.data.attributes.default) {
                    context.commit('pushCategories', { entities: [response.data.data] })
                    return
                }
                return context.dispatch('fetchCategories')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        deleteCategory(context, payload) {
            context.commit('incrementLoading')
            return axios.delete(context.state.baseUrl + '/categories/' + payload.id).then(() => {
                context.commit('removeCategory', payload)
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },

        fetchCategoryPatterns(context, payload) {
            context.commit('incrementLoading')
            context.commit('clearCategoryPatterns')
            return axios.get(context.state.baseUrl + '/categories/' + payload.id + '/patterns').then(response => {
                context.commit('pushCategoryPatterns', { entities: response.data.data })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        createCategoryPattern(context, payload) {
            context.commit('incrementLoading')
            return axios.post(context.state.baseUrl + '/categories/' + payload.categoryId + '/patterns', {
                data: {
                    type: 'categoryPatterns',
                    attributes: payload.pattern.attributes
                }
            }).then(response => {
                context.commit('pushCategoryPatterns', { entities: [response.data.data] })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        updateCategoryPattern(context, payload) {
            context.commit('incrementLoading')
            return axios.patch(context.state.baseUrl + '/categoryPatterns/' + payload.pattern.id, {
                data: {
                    type: 'categoryPatterns',
                    id: payload.pattern.id,
                    attributes: payload.pattern.attributes
                }
            }).then(response => {
                context.commit('decrementLoading')
                context.commit('pushCategoryPatterns', { entities: [response.data.data] })
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        deleteCategoryPattern(context, payload) {
            context.commit('incrementLoading')
            return axios.delete(context.state.baseUrl + '/categoryPatterns/' + payload.id).then(() => {
                context.commit('removeCategoryPattern', payload)
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },

        fetchAccounts(context) {
            context.commit('incrementLoading')
            context.commit('clearAccounts')
            return axios.get(context.state.baseUrl + '/accounts').then(response => {
                context.commit('pushAccounts', { entities: response.data.data })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        createAccount(context, payload) {
            if (payload.attributes.currency) {
                payload.attributes.currency = payload.attributes.currency.toUpperCase()
            }

            context.commit('incrementLoading')
            return axios.post(context.state.baseUrl + '/accounts', {
                data: {
                    type: 'accounts',
                    attributes: payload.attributes
                }
            }).then(response => {
                context.commit('decrementLoading')
                context.commit('pushAccounts', { entities: [response.data.data] })
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        updateAccount(context, payload) {
            if (payload.attributes.currency) {
                payload.attributes.currency = payload.attributes.currency.toUpperCase()
            }

            context.commit('incrementLoading')
            return axios.patch(context.state.baseUrl + '/accounts/' + payload.id, {
                data: {
                    type: 'accounts',
                    id: payload.id,
                    attributes: payload.attributes
                }
            }).then(response => {
                context.commit('decrementLoading')
                context.commit('pushAccounts', { entities: [response.data.data] })
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        deleteAccount(context, payload) {
            context.commit('incrementLoading')
            return axios.delete(context.state.baseUrl + '/accounts/' + payload.id).then(() => {
                context.commit('removeAccount', payload)
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        loadAccount(context, payload) {
            if (context.getters.findCategory(payload.id)) return
            context.commit('incrementLoading')
            return axios.get(context.state.baseUrl + '/accounts/' + payload.id).then(response => {
                context.commit('pushAccounts', { entities: [response.data.data] })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },

        importStatements(context, payload) {
            const formData = new FormData()
            for (let i = 0; i < payload.files.length; i++) {
                formData.append('file' + i, payload.files[i])
            }

            context.commit('incrementLoading')
            return axios.post(context.state.baseUrl + '/utils/import/' + payload.importer, formData, {
                headers: {
                    'Content-Type': `multipart/form-data`
                }
            }).then(() => {
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        fetchImporters(context) {
            context.commit('incrementLoading')
            return axios.get(context.state.baseUrl + '/utils/importers').then(response => {
                context.commit('setImporters', { importers: response.data })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },

        fetchTags(context) {
            context.commit('clearTags')
            context.commit('incrementLoading')
            return axios.get(context.state.baseUrl + '/tags').then(response => {
                context.commit('pushTags', { entities: response.data.data })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        createTag(context, payload) {
            context.commit('incrementLoading')
            return axios.post(context.state.baseUrl + '/tags', {
                data: {
                    type: 'tags',
                    attributes: payload.attributes,
                    relationships: payload.relationships
                }
            }).then(response => {
                context.commit('decrementLoading')
                context.commit('pushTags', { entities: [response.data.data] })
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        loadTag(context, payload) {
            if (context.getters.findTag(payload.id)) return
            context.commit('incrementLoading')
            return axios.get(context.state.baseUrl + '/tags/' + payload.id).then(response => {
                context.commit('pushTags', { entities: [response.data.data] })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        updateTag(context, payload) {
            context.commit('incrementLoading')
            return axios.patch(context.state.baseUrl + '/tags/' + payload.id, {
                data: {
                    type: 'tags',
                    id: payload.id,
                    attributes: payload.attributes,
                    relationships: payload.relationships
                }
            }).then(response => {
                context.commit('decrementLoading')
                context.commit('pushTags', { entities: [response.data.data] })
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        deleteTag(context, payload) {
            context.commit('incrementLoading')
            return axios.delete(context.state.baseUrl + '/tags/' + payload.id).then(response => {
                context.commit('decrementLoading')
                context.commit('removeTag', payload)
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },

        fetchTagPatterns(context, payload) {
            context.commit('incrementLoading')
            context.commit('clearTagPatterns')
            return axios.get(context.state.baseUrl + '/tags/' + payload.id + '/patterns').then(response => {
                context.commit('pushTagPatterns', { entities: response.data.data })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        createTagPattern(context, payload) {
            context.commit('incrementLoading')
            return axios.post(context.state.baseUrl + '/tags/' + payload.tagId + '/patterns', {
                data: {
                    type: 'tagPatterns',
                    attributes: payload.pattern.attributes
                }
            }).then(response => {
                context.commit('pushTagPatterns', { entities: [response.data.data] })
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        updateTagPattern(context, payload) {
            context.commit('incrementLoading')
            return axios.patch(context.state.baseUrl + '/tagPatterns/' + payload.pattern.id, {
                data: {
                    type: 'tagPatterns',
                    id: payload.pattern.id,
                    attributes: payload.pattern.attributes
                }
            }).then(response => {
                context.commit('decrementLoading')
                context.commit('pushTagPatterns', { entities: [response.data.data] })
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },
        deleteTagPattern(context, payload) {
            context.commit('incrementLoading')
            return axios.delete(context.state.baseUrl + '/tagPatterns/' + payload.id).then(() => {
                context.commit('removeTagPattern', payload)
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        },

        applyPatterns(context) {
            context.commit('incrementLoading')
            return axios.post(context.state.baseUrl + '/utils/applyPatterns').then(() => {
                context.commit('decrementLoading')
            }).catch(error => {
                context.commit('addToast', { type: 'error', message: error.message })
                context.commit('decrementLoading')
                throw error;
            })
        }
    },
    getters: {
        getStatements: (state) => (from, to) => {
            const statements = Object.values(state.statements).filter(it => it.attributes.valueDate >= from && it.attributes.valueDate <= to)
            statements.sort((a, b) => {
                const valueDateA = a.attributes.valueDate
                const valueDateB = b.attributes.valueDate

                if (valueDateA === valueDateB) return b.id - a.id
                return valueDateA < valueDateB ? 1 : -1;
            })
            return statements
        },

        findStatement: (state) => (id) => state.statements[id],

        getCategories: (state) => () => {
            const categories = Object.values(state.categories)
            categories.sort((a, b) => {
                const nameA = a.attributes.name
                const nameB = b.attributes.name
                if (nameA === nameB) return b.id - a.id
                return nameA < nameB ? -1 : 1;
            })
            return categories
        },

        findCategory: (state) => (id) => state.categories[id],

        getAccounts: (state) => () => Object.values(state.accounts),

        findAccount: (state) => (id) => state.accounts[id],

        getCategoryPatterns: (state) => (id) => {
            return Object.values(state.categoryPatterns).filter(it => it.relationships.category.data.id == id)
        },

        getTags: (state) => () => {
            const tags = Object.values(state.tags)
            tags.sort((a, b) => {
                const nameA = a.attributes.name
                const nameB = b.attributes.name
                if (nameA === nameB) return b.id - a.id
                return nameA < nameB ? -1 : 1;
            })
            return tags
        },

        findTag: (state) => (id) => state.tags[id],

        getTagPatterns: (state) => (id) => {
            console.log(state.tagPatterns)
            return Object.values(state.tagPatterns).filter(it => it.relationships.tag.data.id == id)
        },
    }
}

export default createStore(store)
