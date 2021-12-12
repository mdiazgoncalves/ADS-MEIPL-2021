import {createWebHistory, createRouter} from "vue-router";
import Home from "@/views/Home.vue";
import Classes from "@/views/Classes";
import Branches from "@/views/Branches";
import Merge from "@/views/Merge";

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
    {
        path: "/branches",
        name: "Branches",
        component: Branches,
        meta: {
            breadcrumbs: [
                {
                    name: "Branches",
                    href: "/branches"
                }
            ]
        },
    },
    {
        path: "/branches/merge/:branch",
        name: "Merge",
        component: Merge,
    },
];

const router = createRouter({
    routes,
    history: createWebHistory(),
});

export default router;
