import {createWebHistory, createRouter} from "vue-router";
import Home from "@/views/Home.vue";
import Classes from "@/views/Classes";

const routes = [
    {
        path: "/",
        name: "Home",
        component: Home,
    },
    {
        path: "/classes",
        name: "Classes",
        component: Classes,
        meta: {
            breadcrumbs: [
                {
                    name: "Classes",
                    href: "/classes"
                }
            ]
        },
    },
];

const router = createRouter({
    routes,
    history: createWebHistory(),
});

export default router;
