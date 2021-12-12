<template>
  <ClassCardsGrid :classes="classes" @delete="deleteClass" @add="addClass"/>
</template>

<script>
import ClassCardsGrid from "@/components/ClassCardsGrid";
import {inject, onMounted, ref, watch} from "vue";
import {useStore} from "vuex";

export default {
  name: "Classes",
  components: {ClassCardsGrid},
  setup() {
    const axios = inject('axios');
    const store = useStore()
    const classes = ref([]);

    const fetchClasses = async (branch) => {
      classes.value = []
      await store.dispatch('setLoading', "Loading classes…");
      const response = branch ? await axios.get(`https://knowledge-base-ads-test2.herokuapp.com/classes?branch=${branch}`) : await axios.get("https://knowledge-base-ads-test2.herokuapp.com/classes?branch=main");
      console.log(response);
      await store.dispatch('setCommit', response.data.latestCommit);
      const classesToAdd = response.data.data;
      while (classesToAdd.length > 0) {
        console.log(classesToAdd)
        const cls = classesToAdd.shift()
        classes.value.push(cls)
        if (cls.subclasses && cls.subclasses.length > 0) {
          cls.subclasses.forEach(it => {
            it["superClass"] = cls
            classesToAdd.push(it);
          })
        }
      }
      await store.dispatch('setLoading', false);
      await store.dispatch('setLoading', {loadingText: "Loading classes…", isLoading: false});
    }

    onMounted(async () => await fetchClasses(store.getters.branch))

    watch(() => store.getters.branch, async (branch) => {
      await fetchClasses(branch);
    })

    const deleteClass = async (className) => {
      await store.dispatch('setLoading', `Deleting class ${className}…`);
      try {
        const response = await axios.delete(`https://knowledge-base-ads-test2.herokuapp.com/class/${className}?branch=${store.getters.branch}&commit=${store.getters.commit}`)
        console.log(response)
        await fetchClasses(store.getters.branch)
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingText: `Deleting class ${className}…`, isLoading: false});
    }

    const addClass = async ({className, superClass}) => {
      await store.dispatch('setLoading', `Adding class ${className}…`);
      try {
        const response = await axios.post(`https://knowledge-base-ads-test2.herokuapp.com/class/${className}?branch=${store.getters.branch}&commit=${store.getters.commit}`, {superClassName: superClass === undefined || superClass.length === 0 ? null : superClass})
        console.log(response)
        await fetchClasses(store.getters.branch)
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingText: `Adding class ${className}…`, isLoading: false});
    }

    return {
      classes,
      deleteClass,
      addClass,
    }
  },
}
</script>