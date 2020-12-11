import sidebar from './components/sidebar.js'
const { onMounted } = Vue

export default {
    components: {
        sidebar
    },
    template: `
        <div>
          <sidebar />
          <main>
              Main section
          </main>
          <footer>Footer</footer>
        </div>
    `,
    setup() {
        onMounted(() => {
            console.log('Mounting');
        })

        console.log('Created app');
    }
}