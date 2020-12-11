import app from './src/app.js'

// const lightCSS = document.querySelector('link[href*="light.min.css"]')
// const darkCSS = document.querySelector('link[href*="dark.min.css"]')

// !localStorage['colorTheme'] && (localStorage['colorTheme'] = 'light')
// lightCSS.setAttribute('rel', (localStorage['colorTheme'] == 'light') ? 'stylesheet' : 'stylesheet alternate')
// darkCSS.setAttribute('rel', (localStorage['colorTheme'] == 'light') ? 'stylesheet alternate' : 'stylesheet')

const { createApp } = Vue;
createApp(app).mount('#app');




async function getCollectionNames() {
    let klassNames = await fetch('/rest/klassNames')
    klassNames = await klassNames.json();
    console.log(klassNames);

    for(let klass of klassNames) {
        await getCollection(klass)
    }
}

async function getCollection(coll) {
    let res = await fetch('/rest/' + coll)
    console.log(await res.json());
}
