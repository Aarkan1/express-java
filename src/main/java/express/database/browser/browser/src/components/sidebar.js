const { ref } = Vue
const { useStore } = Vuex

export default {
    template: `
        <aside>
            Sidebar

            <slot></slot>

            <div>
                Theme
                <label class="switch" @click.stop="toggleTheme">
                    <input v-model="isLightTheme" type="checkbox">
                    <span class="slider round"></span>
                </label>
                {{ isLightTheme ? 'Light' : 'Dark' }}
            </div>
        </aside>
    `,
    setup() {
        const store = useStore()

        console.log(store.state.activeKlass);

        const isLightTheme = ref(localStorage['colorTheme'] == 'light')

        const toggleTheme = () => {
            setTimeout(() => { 
                const lightCSS = document.querySelector('link[href*="light.min.css"]')
                const darkCSS = document.querySelector('link[href*="dark.min.css"]')
                
                lightCSS.setAttribute('rel', isLightTheme.value ? 'stylesheet' : 'stylesheet alternate')
                darkCSS.setAttribute('rel', isLightTheme.value ? 'stylesheet alternate' : 'stylesheet')
                
                localStorage['colorTheme'] = isLightTheme.value ? 'light' : 'dark'
            }, 0);
        }

        return {
            isLightTheme,
            toggleTheme
        }
    }
}