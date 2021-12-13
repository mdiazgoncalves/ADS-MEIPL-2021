<template>
  <div id="merge-container">
    <div class="main">
      <header>
        <h2>Main branch</h2>
        <div class="actions">
          <div class="action copy" @click="copy(mainOwl)">Copy</div>
          <div class="action" @click="resultOwl = mainOwl">&#x21AA;</div>
        </div>
      </header>
      <code class="code-block">{{ mainOwl }}</code>
    </div>
    <div class="result">
      <header>
        <h2>Result</h2>
        <div class="actions">
          <button class="primary" id="merge-btn" @click="merge()">Merge</button>
        </div>
      </header>
      <textarea class="code-block" v-model="resultOwl"/>
    </div>
    <div class="branch">
      <header>
        <div class="actions">
          <div class="action" @click="resultOwl = branchOwl">&#x21A9;</div>
          <div class="action copy" @click="copy(branchOwl)">Copy</div>
        </div>
        <h2>{{ branch }}</h2>
      </header>
      <code class="code-block">{{ branchOwl }}</code>
    </div>
  </div>
</template>

<script>
import {useRoute, useRouter} from "vue-router";
import {computed, inject, onActivated, onMounted, ref, watch} from "vue";
import {useStore} from "vuex";

export default {
  name: "Merge",
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const branch = computed(() => route.params.branch);
    const commit = ref("")
    const mainOwl = ref("")
    const branchOwl = ref("")
    const resultOwl = ref("")
    const axios = inject('axios');

    onActivated(() => {
      if (store.getters.token == null || !branch.value) {
        router.push("/");
      }
    })

    const fetchOwls = async () => {
      if (store.getters.token == null || !branch.value) return;
      mainOwl.value = ""
      branchOwl.value = ""
      resultOwl.value = ""
      await store.dispatch('setLoading', {loadingText: "Loading OWL…", loadingId: 1300, isLoading: true});
      try {
        const responses = await Promise.all([
          axios.get(`https://knowledge-base-ads-test2.herokuapp.com/branch/main/owl?token=${store.getters.token}`),
          axios.get(`https://knowledge-base-ads-test2.herokuapp.com/branch/${branch.value}/owl?token=${store.getters.token}`),
          axios.get(`https://knowledge-base-ads-test2.herokuapp.com/branch/${branch.value}/latest?token=${store.getters.token}`),
        ]);
        console.log(responses);
        mainOwl.value = responses[0].data
        branchOwl.value = responses[1].data
        commit.value = responses[2].data.latestCommit
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingId: 1300, isLoading: false});
    }

    watch([() => store.getters.token, branch], async () => {
      await fetchOwls();
    })

    onMounted(async () => await fetchOwls())

    const copy = async (text) => {
      await navigator.clipboard.writeText(text)
    }

    const merge = async () => {
      await store.dispatch('setLoading', {loadingText: `Merging branch ${branch.value}…`, loadingId: 1500, isLoading: true});
      try {
      const response = await axios.post(`https://knowledge-base-ads-test2.herokuapp.com/branch/${branch.value}/mergeowl?token=${store.getters.token}&commit=${commit.value}`, resultOwl.value, {
        headers: {
          "Content-Type": "application/xml"
        }
      })
      console.log(response);
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingId: 1500, isLoading: false});
      router.back()
    }

    return {
      mainOwl,
      branchOwl,
      resultOwl,
      branch,
      copy,
      merge,
    }
  }
}
</script>

<style scoped>
#merge-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  width: 90%;
  margin: 0 auto;
  gap: 16px;
}

#merge-container > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 9em;
}

#merge-container > div > header {
  display: flex;
  gap: 24px;
  padding-left: 8px;
  padding-right: 8px;
  justify-content: space-between;
  align-items: center;
  height: 36px;
}

#merge-container > div > header > .actions {
  display: flex;
  gap: 8px;
}

#merge-container > div > header > .actions > .action {
  cursor: pointer;
  padding: 4px 10px 0;
  border: solid 1px var(--primary);
  color: var(--primary);
  border-radius: 20px;
}

#merge-container > div > header > .actions > .action:hover {
  background-color: var(--primary);
  border: solid 1px transparent;
  color: white;
}

.code-block {
  display: block;
  position: relative;
  background-color: white;
  /*box-shadow: rgba(0, 0, 0, 0.04) 0 3px 5px;*/
  border-radius: 4px;
  border: 1px solid #d3d3d3;
  padding: 16px;
  white-space: pre;
  overflow-x: auto;
}

.result {
  display: flex;
  flex-direction: column;
}

.result textarea {
  flex: 1;
}

#merge-btn {
  height: 32px;
  border-radius: 16px;
}

.copy {
  font-size: 13px;
}
</style>