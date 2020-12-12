import sidebar from './components/sidebar.js'
import collections from './components/collections.js'
import * as pages from './components/pages.js'


const { watchEffect, ref } = Vue

export default {
    components: Object.assign({ sidebar, collections }, pages),
    template: `
        <div>
          <sidebar>
              <button @click="page = ''">Collections</button>
              <button v-for="item in pages" :key="item.name" @click="page = item.name.toLowerCase()">{{ item.name }}</button>
          </sidebar>
          <main>
            <component :is="page || 'collections'"></component>
          </main>
          <footer>Footer</footer>
        </div>
    `,
    setup() {
        const page = ref(null)

        //url management
        watchEffect(() => {
            const urlpage = window.location.pathname.split("/").pop()
            
            if (page.value == null) {
                page.value = urlpage
            }

            if (page.value != urlpage) { 
                const url = page.value ? page.value : './'
                window.history.pushState({ url }, '', url) 
            }

            window.onpopstate = () => {
                page.value = window.location.pathname.split("/").pop()
            }
        })

        return {
            page, pages
        }
    }
}