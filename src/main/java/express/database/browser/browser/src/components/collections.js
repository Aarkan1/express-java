const { ref, computed, onMounted } = Vue
const { useStore } = Vuex

export default {
    name: 'Collections',
    template: `
        <div>
            Collections
            <div id="json"></div>
        </div>
    `,
    setup() {
        const store = useStore()
        const jsonViewer = new JSONViewer()

        // const todos = computed(() => store.state.collections || [])

        const users = ref([
            {
                name: 'Johan',
                age: 31,
                pets: [
                    {
                        name: 'Tyson',
                        color: 'Gray'
                    },
                    {
                        name: 'Cocos',
                        color: 'Orange'
                    }
                ]
            },
            {
                name: 'Evelna',
                age: 31,
                pets: [
                    {
                        name: 'Tyson',
                        color: 'Gray'
                    },
                    {
                        name: 'Cocos',
                        color: 'Orange'
                    }
                ]
            },
            {
                name: 'Loke',
                age: 6,
                pets: [
                    {
                        name: 'Tyson',
                        color: 'Gray'
                    },
                    {
                        name: 'Cocos',
                        color: 'Orange'
                    }
                ]
            }
        ])

        onMounted(() => {
            document.querySelector("#json").appendChild(jsonViewer.getContainer())
            jsonViewer.showJSON(users.value, -1, 2)
        })

        return {
            users
        }
    }
}