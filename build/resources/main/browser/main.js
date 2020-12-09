console.log('Browsing collections');

async function getCollection(coll) {
    let res = await fetch('/rest/' + coll)
    console.log(await res.json());
}

getCollection("BlogPost")