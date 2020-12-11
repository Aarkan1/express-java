const { createStore } = Vuex

const state = {
    activeKlass: '',
    collNames: [],
    collections: {}
}

const mutations = {
    setActiveKlass(state, klass) {
        state.activeKlass = klass
    },
    setCollNames(state, colls) {
        state.collNames = colls
    },
    setCollections(state, data) {
        Vue.set(state.collections, data.coll, data.collection)
    }
}

const actions = {
    async getCollNames(store) {
        let collNames = await fetch('/rest/collNames')
        collNames = await collNames.json();
        console.log(collNames);

        store.commit('setActiveKlass', collNames[0])
        store.commit('setCollNames', collNames)

        for(let coll of collNames) {
            store.dispatch('getCollection', coll)
        }
    },
    async getCollection(store, coll) {
        let collection = await fetch('/rest/' + coll)
        collection = await res.json()
        console.log(collection);
        store.commit('setCollections', {coll ,collection})
    }
}

export default createStore({state, mutations, actions})
