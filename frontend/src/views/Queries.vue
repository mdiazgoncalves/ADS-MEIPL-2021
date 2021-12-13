<template>
  <section id="queries-page">
    <form class="query-input-container" @submit.prevent="execute()">
      <input type="text" class="query-input" placeholder="Input your query [e.g. #Bebida(?x) -> sqwrl:select(?x)]"
             v-model="query"/>
      <input type="submit" value="Execute" class="primary"/>
    </form>

    <div id="individuals-cards">
      <div v-for="individual in individuals" :key="individual">
        <router-link :to="`/individuals#${individual}`" class="card">
          <header>
            <div class="individual-name-delete">
              <h2>{{ individual }}</h2>
            </div>
          </header>
        </router-link>
      </div>
    </div>
  </section>
</template>

<script>
import {inject, ref} from "vue";
import {useStore} from "vuex";

export default {
  name: "Queries",
  setup() {
    const axios = inject('axios');
    const store = useStore()
    const query = ref("#Bebida(?x) -> sqwrl:select(?x)")
    const individuals = ref([])

    const execute = async () => {
      individuals.value = []
      await store.dispatch('setLoading', {loadingText: "Executing query…", loadingId: 1800, isLoading: true});
      try {
        const url = store.getters.branch ? `${process.env.VUE_APP_BACKEND}/query?branch=${store.getters.branch}` : `${process.env.VUE_APP_BACKEND}/query?branch=main`
        const response = await axios.post(url, query.value, {
          headers: {
            "Content-Type": "text/plain"
          }
        })
        console.log(response)
        individuals.value = response.data.data
      } catch (e) {
        console.log(e.response)
      }
      await store.dispatch('setLoading', {loadingId: 1800, isLoading: false});
    }

    return {
      query,
      execute,
      individuals,
    }
  }
}
</script>

<style scoped>
#queries-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.query-input-container {
  width: 70%;
  margin: 0 auto;
  display: flex;
  gap: 8px;
}

.query-input {
  flex: 1;
}

#individuals-cards {
  width: 70%;
  margin: 0 auto;
  display: flex;
  flex-flow: row wrap;
  align-items: stretch;
  align-content: stretch;
  gap: 8px;
}

.card h2 {
  text-align: center;
}

.card {
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  /*box-shadow: rgba(0, 0, 0, 0.04) 0 3px 5px;*/
  border-radius: 32px;
  border: 1px solid #d3d3d3;
  padding: 16px;
  transition: box-shadow 0.2s, border-color 0.2s, color 0.2s;
}

.card:hover {
  box-shadow: rgba(0, 0, 0, 0.08) 0 3px 5px;
  border-color: var(--primary);
  color: var(--primary);
}

.card hr {
  display: block;
  margin: 8px 0;
  border-top: solid 1px #d2d2d2;
  width: 100%;
}

.card header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  justify-content: center;
}

.individual-name-delete {
  display: flex;
  justify-content: center;
}

.class-name {
  font-size: 12px;
  color: #494949;
  text-transform: uppercase;
  text-align: center;
}

.delete {
  color: #c93b3b;
  background-color: #f1f1f1;
  border-radius: 16px;
  padding: 0 8px;
  margin-left: 8px;
}

.delete:hover {
  cursor: pointer;
  color: #f1f1f1;
  background-color: #c93b3b;
}

.title {
  padding-top: 8px;
  padding-bottom: 8px;
  text-transform: uppercase;
  color: #3d3d3d;
  font-size: 12px;
}

.relationships {
  list-style-type: none;
}

.relationship:not(:last-child) {
  padding-bottom: 6px;
}

.relationship-name {
  font-weight: 500;
}

.relationship {
  font-size: 15px;
  display: flex;
  gap: 8px;
}
</style>